/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import static com.mycompany.client.ServerOpCodes.FRIEND_LIST;
import static com.mycompany.client.ServerOpCodes.NO_FRIENDS;
import static com.mycompany.client.ServerOpCodes.NO_PENDINGS;
import static com.mycompany.client.ServerOpCodes.PENDING_LIST;
import javax.swing.*;
import java.io.*;
import java.util.Base64;

/**
 *
 * @author linas
 */

public class Graph
{
    private Protocol tcpClient;
    
    private JFrame frameLogReg;
    private JFrame chatFrame;
    
    private LoginRegistrationUI logRegUI;
    private JFrame frameMain;
    private MainUI mainUI;
    private SearchUI searchUI;
    private ChatUI messageUI;
    
    private byte[] session_key;
    private boolean isSecondPacket = false;
    
    String me;
    
    Graph()
    {
        
        tcpClient = new Protocol();
        try
        {
           tcpClient.setListener(null);
           tcpClient.setListener(msg ->
           {
               SwingUtilities.invokeLater(() ->
               {
                   System.out.println("DEBUGUI: "+msg);
                   String message = msg;
                   try
                   {
                            if(isSecondPacket)
                            {
                               message = Security.decrypt(msg, session_key);
                            }
                    switch(separateOpCode(message))
                    {
                        
                        case HANDSHAKE -> handshakeKey(msg);
                        
                        case LOGIN_OK -> 
                            {
                                this.me = logRegUI.getMyUsername();
                                mainFrame();
                            }
                        case LOGIN_FAIL -> {if(logRegUI!=null)logRegUI.screenState("Wrong UserID or password");}
                        case TOO_MANY_LOG_ATTEMPTS -> {if(logRegUI!=null)logRegUI.screenState("Too many attempts, relaunch app");}
                        case ALREADY_LOGGED -> {if(logRegUI!=null)logRegUI.screenState("User already logged in");}
                        
                        case FRIEND_LIST -> {if(mainUI!=null) mainUI.showFriendList(message);}
                        case NO_FRIENDS -> {if(mainUI!=null) mainUI.showEmpty(true);}
                        case PENDING_LIST -> {if(mainUI!=null) mainUI.showFriendRequests(message);}
                        case NO_PENDINGS -> {if(mainUI!=null) mainUI.showEmpty(false);}
                        
                        case REQUEST_RECEIVED -> {if(mainUI!=null) mainUI.getPendingList();}

                        case REGISTER_OK -> {if(logRegUI!=null)logRegUI.screenState("Registration successful");}
                        case REGISTER_FAIL -> {if(logRegUI!=null)logRegUI.screenState("Username alrady taken.");}
                        case TOO_MANY_REG_ATTEMPTS -> {if(logRegUI!=null)logRegUI.screenState("Too many attempts, relaunch app");}
                        case NO_NUMBER_AT_START -> {if(logRegUI!=null)logRegUI.screenState("Start with number error");}
                        case USER_ALREADY_EXISTS -> {if(logRegUI!=null)logRegUI.screenState("User already exists");}
                        
                        case USER_FOUND -> {if(searchUI!=null) searchUI.doesUserExist(true);}
                        case USER_NOT_FOUND -> {if(searchUI!=null) searchUI.doesUserExist(false);}
                       
                        case CHAT_HISTORY -> {if(messageUI!=null) messageUI.getMessages(message);}
                        case FILES_HISTORY -> {if(messageUI != null) messageUI.getFiles(message);}
                        
                        case LAST_MESSAGE -> {if(messageUI!=null) messageUI.receiveLastMessage(message);}
                        case LAST_FILE -> {if(messageUI!=null) messageUI.receiveLastFile(message);}
                        case DOWNLOADED -> {if(messageUI!=null) messageUI.downloadFile(message);}
                        
                        case IM_ACTIVE -> {if(mainUI!=null) mainUI.online(message);}
                        
                        case DISCONNECTED ->
                                    {
                                        if(logRegUI != null) logRegUI.screenState("Disconnected");

                                        if(chatFrame != null)
                                        {
                                            chatFrame.dispose();
                                            chatFrame = null;
                                            messageUI = null;
                                        }
                                        session_key = null;
                                        isSecondPacket = false;
                                    }
                        case OFFLINE -> {if(mainUI!=null) mainUI.offline(message);}          
                        
                        default -> System.out.println(ServerOpCodes.UNKNOWN_REQUEST.name()); 
                    }
                     }catch(Exception e){
           System.out.println("RSA error: " + e.getMessage());
        }
               });
              
            });
           tcpClient.connect();
           
        }catch(IOException e){
           System.out.println("TCP error: " + e.getMessage());
        }
        initUI();
        
        
    }

    private void initUI()
    {
        frameLogReg = new JFrame("Client");
        frameLogReg.setSize(300,180);
        frameLogReg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameLogReg.setLocationRelativeTo(null);

        logRegUI = new LoginRegistrationUI(frameLogReg);
        
        logRegPacket();
    }
    
    private void mainFrame()
    {
        frameLogReg.dispose();
        
        frameMain = new JFrame("Client");

        mainUI = new MainUI(frameMain);
        
        mainUI.setLoginListener(() -> initUI());
        mainUI.setSearchListener(() -> searchFrame());
        mainUI.setChatListener(friendName -> chatFrame(friendName,me));
        
        frameMain.setSize(600,480);
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameMain.setLocationRelativeTo(null);
        frameMain.setVisible(true);
        
        newFriendAccepted();
        askForDisconnectPacket();
        setupMainPackets();  
          
        mainUI.getFriendsList();
        mainUI.getPendingList();
        try
        {
            imActive();
        }catch(Exception e){
            System.out.println("Problem with imActive.");
        }
        
    }
    
    private void searchFrame()
    {
        frameMain = new JFrame("Search");
        searchUI = new SearchUI(frameMain);
        searchUI.setBackListener(() -> mainFrame());
        
        frameMain.setSize(600,480);
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameMain.setLocationRelativeTo(null);
        frameMain.setVisible(true);
        
        askForSearchPacket();
        askForFriendRequestPacket();
       
    }
    
    private void chatFrame(String friendName,String myUsername)
    {
        if (chatFrame != null && chatFrame.isDisplayable())
        {
            messageUI.loadFriend(friendName);

            chatFrame.setTitle("Chat - " + friendName);

            updateChatMessages();

            chatFrame.toFront();
            chatFrame.requestFocus();

            return;
        }
        chatFrame = new JFrame("Chat");
    
        messageUI = new ChatUI(chatFrame, friendName,myUsername);
        
        chatFrame.setSize(500, 600);
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setVisible(true);
    
        chatFrame.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) 
            {
                chatFrame = null;
            }
        });
        
        updateChatMessages();
        updateChatFiles();
        sendMessageToUser();      
    }
    
    private ServerOpCodes separateOpCode(String message)
    {
        System.out.println("Received: " + message);
                
        String[] parts = message.split(":");
        if (parts.length < 1) return ServerOpCodes.INVALID_FORMAT;

        try
        {
            return ServerOpCodes.valueOf(parts[0]);
                    
        }catch(IllegalArgumentException e){
            System.out.println("Cannot convert String to Enum. " + e.getMessage());
            return ServerOpCodes.INVALID_FORMAT;
        }
    }
    
    void handshakeKey(String message) throws Exception
    {
        this.session_key = Security.keyGenerator();
            System.out.println(Base64.getEncoder().encodeToString(session_key));
        
        tcpClient.send(Security.encryptData(this.session_key,message));
    }
    
    void imActive() throws Exception
    { 
        tcpClient.send(Security.encrypt(ClientOpCodes.SEND_IM_ACTIVE.name(),session_key));
    }
    
    private void logRegPacket()
    {
        logRegUI.setPacketListener(packet ->
        {
            System.out.println("session_key = " + (session_key == null ? "NULL" : "SET"));

            if(session_key == null)
            {
                System.out.println("Handshake not completed");
                return;
            }
            tcpClient.send(Security.encrypt(packet,session_key));
        });
    }
    
    private void setupMainPackets()
    {
        mainUI.setPacketListener(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void askForSearchPacket()
    {
        searchUI.setPacketListener(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void askForDisconnectPacket()
    {
        mainUI.setDisconnectListener(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void askForFriendRequestPacket()
    {
        searchUI.askToBeFriend(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void newFriendAccepted()
    {
        mainUI.addNewFriend(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void sendMessageToUser()
    {
        messageUI.setSendMessage(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void updateChatMessages()
    {
        mainUI.updateChatFrameMsg(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
    
    private void updateChatFiles()
    {
        mainUI.updateChatFrameFile(packet -> tcpClient.send(Security.encrypt(packet,session_key)));
    }
   
}