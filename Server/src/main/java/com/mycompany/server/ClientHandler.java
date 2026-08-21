package com.mycompany.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable
{
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
 
    private String connectedUser;
    private java.security.KeyPair rsa_key;
    private byte[] session_key;
    private boolean isSecondPacket = false;
    private boolean isDisconnected = true;
    
    private int loginAttempts = 0;
    private int registerAttempts = 0;
    private static final int MAX_ATTEMPTS = 10;

    ClientOpCodes packetsFromClient;
    
    ClientHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            try
            {
                sendFirstPacket();
               
            }catch(Exception e){                   
                    System.out.println("Something wrong with RSA."+e.getMessage());
            }
              
            String encodedMessage;

            while((encodedMessage = input.readLine()) != null)
            {
                System.out.println("Received encrypted: " + encodedMessage);
                try
                {  
                    if(isSecondPacket)
                    {
                        encodedMessage = Cypher.decrypt(encodedMessage,session_key);
                    }
                    else
                    {
                        isSecondPacket = true;
                    }
                    
                    System.out.println("Received decrypted: " + encodedMessage);
                    
                    String[] parts = encodedMessage.split(":");
                    if (parts.length < 1) continue; 

                    try
                    {
                        packetsFromClient = ClientOpCodes.valueOf(parts[0]);
                    
                    }catch(IllegalArgumentException e){                    
                        sendSystemMessage("Unknown action");
                    }

                    switch (packetsFromClient)
                    {
                        case SEND_KEY -> getSessionKey(parts);
                    
                        case LOGIN_REQUEST -> handleLogin(parts);
                        case SEND_IM_ACTIVE -> imActive(parts);
                        case REGISTER_REQUEST -> handleRegister(parts);
                        case DISCONNECT_REQUEST -> disconnect(parts);
                        case GET_FRIEND_LIST -> sendFriendList(parts);
                        case GET_PENDING_LIST -> sendPendingRequests(parts);
                        case SEARCH_USER -> handleSearch(parts);
                        case SEND_FRIEND_REQUEST -> pendingForFriend(parts);
                        case ACCEPT_FRIEND_REQUEST -> sendAcceptFriend(parts);
                        case DECLINE_FRIEND_REQUEST -> sendDeclineFriend(parts);
                        case GET_CHAT_HISTORY -> updateChatMsg(parts);
                        case GET_FILES_HISTORY -> updateChatFile(parts);
                        case SEND_MESSAGE -> sendMessageToUser(parts);
                        case SEND_FILE -> sendFileToUser(parts);
                        case DOWNLOAD_FILE -> downloadFile(parts);
                    }
                }catch(Exception e){
                    System.out.println("Something wrong with RSA decryption."+e.getMessage());
                }
            }          
        }catch(IOException e){
            System.out.println("Connection error: " + e.getMessage());
        }
        finally
        {
            if(connectedUser != null) Protocol.removeClient(connectedUser);
        
            Mysql db = new Mysql();
            db.setActive(connectedUser,false);
          
            try
            { 
                socket.close();
            }
            catch(IOException ignored){}
        }
    }
    
    private void getSessionKey(String[] parts) throws Exception
    {
        if(parts.length != 2)
        {
            System.out.println("Problem assigning session key.");
            return;
        }
        this.session_key = RSA.decryptKey(parts[1],rsa_key);
        
        System.out.println(Base64.getEncoder().encodeToString(session_key));
    }
    
    private void handleLogin(String[] parts)
    {
        
        if(loginAttempts >= MAX_ATTEMPTS)
        {
            sendSystemMessage(ServerOpCodes.TOO_MANY_LOG_ATTEMPTS.name());
            return;
        }

        loginAttempts++;

        if(parts.length != 3)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }

        String user = parts[1];
        String pass = parts[2];

        ServerOpCodes result = authenticate(user,pass);

        switch(result)
        {
            case LOGIN_OK ->
            {
                loginAttempts = 0;

                Mysql db = new Mysql();
                this.connectedUser = user;
                db.setActive(user, true);

                Protocol.registerClient(user, this);
                
                sendSystemMessage(ServerOpCodes.LOGIN_OK.name());      
            }
            case ALREADY_LOGGED -> sendSystemMessage(ServerOpCodes.ALREADY_LOGGED.name());
            case USER_NOT_FOUND -> sendSystemMessage(ServerOpCodes.USER_NOT_FOUND.name());
            case LOGIN_FAIL -> sendSystemMessage(ServerOpCodes.LOGIN_FAIL.name());
        }
    }
    
    private void imActive(String parts[])
    {
        if(parts.length != 1)
        {
            sendSystemMessage(ServerOpCodes.ACTIVE_FAIL.name());
        }
        Mysql db = new Mysql();
        int userId = db.getUserIdByUsername(connectedUser);
        List<String> friends = db.getFriendListActive(userId);
        for(int i = 0;i<friends.size();i++)
        {
            Protocol.sendMessage(connectedUser,friends.get(i),ServerOpCodes.IM_ACTIVE.name() + ":" + connectedUser);
        }      
    }
    
    private void handleRegister(String[] parts)
    {      
        if(registerAttempts >= MAX_ATTEMPTS)
        {
            sendSystemMessage(ServerOpCodes.TOO_MANY_REG_ATTEMPTS.name());
            return;
        }

        registerAttempts++;

        if(parts.length != 3)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());//////////////////////////////////////////////////////
            return;
        }

        String user = parts[1];
        String pass = parts[2];
        
        if (user.matches("^[0-9].*") || pass.matches("^[0-9].*"))
        {
            sendSystemMessage(ServerOpCodes.NO_NUMBER_AT_START.name());
            return;
        }

        Mysql db = new Mysql();

        if(db.findUser(user) != null)
        {
            sendSystemMessage(ServerOpCodes.USER_ALREADY_EXISTS.name());
            System.out.println("Register failed.");
            return;
        }
        
        byte[] key = Cypher.keyGeneratorForPsw();
        String encPass = Cypher.encrypt(pass, key);

        db.createUser(user, encPass);
        int userId = db.getUserIdByUsername(user);

        if(userId > 0)
        {
        
            db.insertKey(userId, key);

            sendSystemMessage(ServerOpCodes.REGISTER_OK.name());
            System.out.println("Register successful");
        }
        else
        {
            sendSystemMessage(ServerOpCodes.REGISTER_FAIL.name());
        }
    }

    private void handleSearch(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }

        String user = parts[1];

        Mysql db = new Mysql();
        boolean exists = db.findUser(user).equals(user);

        if(exists)
        {
            sendSystemMessage(ServerOpCodes.USER_FOUND.name());
            System.out.println("User found.");
        }
        else
        {
            sendSystemMessage(ServerOpCodes.USER_NOT_FOUND.name());
            System.out.println("Not found.");
        }
    }
    
    private void pendingForFriend(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }

        String friend = parts[1];

        Mysql db = new Mysql();
        db.addFriend(connectedUser,friend);

        System.out.println("WAITING FOR ANSWER FROM USER.");
        sendSystemMessage(ServerOpCodes.FRIEND_REQUEST_SENT.name());
        Protocol.sendMessage(connectedUser,friend,ServerOpCodes.REQUEST_RECEIVED.name());
    }
    
    private void sendFriendList(String[] parts)
    {
        if(parts.length != 1)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }

        Mysql db = new Mysql();
        
        int connectedUserID = db.getUserIdByUsername(this.connectedUser);
        List<String> friends = db.getFriendList(connectedUserID);
     
        if(friends.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_FRIENDS.name());
            return;
        }
        
        String payload = String.join(",",friends);
        sendSystemMessage(ServerOpCodes.FRIEND_LIST.name() + ":" + payload);        
    }
    
    private void sendPendingRequests(String[] parts)
    {
        if(parts.length != 1)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        
        Mysql db = new Mysql();
        List<String> requests = db.getPendingFriendRequests(connectedUser);

        if(requests.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_PENDINGS.name());
            return;
        }
        
        String payload = String.join(",", requests);
        sendSystemMessage(ServerOpCodes.PENDING_LIST.name() + ":" + payload);
    
    }
    
    private void sendAcceptFriend(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        
        String newFriend = parts[1];
        
        Mysql db = new Mysql();
        db.acceptFriendship(connectedUser,newFriend);
        
        sendSystemMessage(ServerOpCodes.FRIEND_REQUEST_ACCEPTED.name());    
    }
    
    private void sendDeclineFriend(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        
        String newFriend = parts[1];
        
        Mysql db = new Mysql();
        db.deletePendingFriendRequest(connectedUser, newFriend);
        
        sendSystemMessage(ServerOpCodes.FRIEND_REQUEST_DECLINED.name());    
    }
     
    private void updateChatMsg(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        String receiver = parts[1];
        
        Mysql db = new Mysql();
        List<String> messages = db.receiveMessages(connectedUser,receiver);
        
        if(messages.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_MESSAGES.name());
            return;
        }
        
        String payload = String.join("||",messages);
        sendSystemMessage(ServerOpCodes.CHAT_HISTORY.name() + ":" + payload);
    }
    private void updateChatFile(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        String receiver = parts[1];
        
        Mysql db = new Mysql();
        List<String> files = db.receiveFiles(connectedUser,receiver);
        
        if(files.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_FILES.name());
            return;
        }
        
        String payload = String.join("||",files);
        sendSystemMessage(ServerOpCodes.FILES_HISTORY.name() + ":" + payload);
    }
    
    private void sendMessageToUser(String[] parts)
    {
        if(parts.length != 3)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        String receiver = parts[1];
        String message = parts[2];
        
        Mysql db = new Mysql();
        db.storeMessage(connectedUser,receiver,message);
        
        sendSystemMessage(ServerOpCodes.MESSAGE_SENT.name());
        updateChatWithLastMessage(receiver);
    }
    
    private void updateChatWithLastMessage(String receiver)
    {   
        Mysql db = new Mysql();
        List<String> messages = db.receiveMessages(connectedUser,receiver);
      
        if(messages.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_MESSAGES.name());
            return;
        }

        String msg = ServerOpCodes.LAST_MESSAGE.name() + ":" + messages.get(messages.size()-1);
        Protocol.sendMessage(connectedUser,receiver,msg);     
    }
    
    private void sendFileToUser(String[] parts)
    {
        if(parts.length != 4)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        String receiver = parts[1];
        String filename = parts[2];
        byte[] fileBytes = Base64.getDecoder().decode(parts[3]);
        
        Mysql db = new Mysql();
        db.storeFiles(connectedUser,receiver,filename,fileBytes);
        
        sendSystemMessage(ServerOpCodes.FILE_SENT.name());
        updateChatWithLastFile(receiver);
    }
    
    private void updateChatWithLastFile(String receiver)
    {   
        Mysql db = new Mysql();
        List<String> files = db.receiveFiles(connectedUser,receiver);
      
        if(files.isEmpty())
        {
            sendSystemMessage(ServerOpCodes.NO_FILES.name());
            return;
        }

        String file = ServerOpCodes.LAST_FILE.name() + ":" + files.get(files.size()-1);
        Protocol.sendFile(connectedUser,receiver,file);     
    }   
    
    private void downloadFile(String[] parts)
    {
        if(parts.length != 2)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        String fileId = parts[1];
        
        Mysql db = new Mysql();
        
        byte[] data = db.downloadFile(fileId);
        
        if(data != null)
        {
            String encoded = Base64.getEncoder().encodeToString(data);

            System.out.println("FILE_DATA:" + encoded);
            sendSystemMessage(ServerOpCodes.DOWNLOADED.name() + ":" + fileId + ":" + encoded);
        }        
    }
    
    private ServerOpCodes authenticate(String username, String password)
    {
        System.out.println("AUTH CALLED");

        Mysql db = new Mysql();

        String stored = db.getPasswordByUsername(username);

        System.out.println("LOGIN TRY: " + username);
        System.out.println("DB VALUE: " + stored);

        String doesExist = db.findUser(username);
        if(doesExist.matches(stored))
        {
            
        }
        if(db.isOnline(username))
        {
            System.out.println("USER ALREADY LOGGED IN.");
            return ServerOpCodes.ALREADY_LOGGED;
        }

        if(stored == null)
        {
            System.out.println("USER DOES NOT EXIST.");
            return ServerOpCodes.LOGIN_FAIL;
        }

        String encryptedInput = getPasswordKeyAndEnc(username, password);

        if(!stored.equals(encryptedInput))
        {
            System.out.println("WRONG PASSWORD.");
            return ServerOpCodes.LOGIN_FAIL;
        }

        System.out.println("LOGIN SUCCESSFUL");
        return ServerOpCodes.LOGIN_OK;
    }
    
    private void disconnect(String[] parts)
    {
        if(parts.length != 1)
        {
            sendSystemMessage(ServerOpCodes.INVALID_FORMAT.name());
            return;
        }
        sendSystemMessage(ServerOpCodes.DISCONNECTED.name()); 
        Mysql db = new Mysql();
        
        Protocol.removeClient(connectedUser);
        db.setActive(connectedUser,false);
        
        int userId = db.getUserIdByUsername(connectedUser);
        List<String> friends = db.getFriendListActive(userId);
        for(int i = 0;i<friends.size();i++)
        {
            Protocol.sendMessage(connectedUser,friends.get(i),ServerOpCodes.OFFLINE.name() + ":" + connectedUser);   
        }   
        
        resetSession(); 

        try
        {
            sendFirstPacket();
        }catch(Exception e){
            System.out.println("Problem with first packet inside disconnect.");
        }

        //sendSystemMessage(ServerOpCodes.DISCONNECTED.name());      
    }
    
    private void firstSystemMessage(String msg)
    {
        System.out.println("FROM ClientHandler to CLIENT -> " + msg);
        try
        {
            output.write(msg);
            output.newLine();
            output.flush();
        }
        catch (IOException e)
        {
            System.out.println("Send error: " + e.getMessage());
        }
    }
    
    protected void sendSystemMessage(String msg)
    {
        System.out.println("FROM ClientHandler to CLIENT -> " + msg);
        try
        {
            Cypher.encrypt(msg, session_key);
            output.write(msg);
            output.newLine();
            output.flush();
        }
        catch (IOException e)
        {
            System.out.println("Send error: " + e.getMessage());
        }
    }
    protected void sendFirstPacket() throws Exception
    {
        if(isDisconnected)
        {
            this.rsa_key = RSA.rsaKey();
            if(socket != null) 
            firstSystemMessage(ServerOpCodes.HANDSHAKE.name()+ ":" + Base64.getEncoder().encodeToString(rsa_key.getPublic().getEncoded()));
            isDisconnected = false;
        }
        
    }
    private void resetSession()
    {
        try
        {
            this.rsa_key = RSA.rsaKey();
        }catch (Exception e){
        System.out.println("RSA reset failed: " + e.getMessage());
        }

        this.session_key = null;
        this.isSecondPacket = false;

        this.connectedUser = null;

        this.loginAttempts = 0;
        this.registerAttempts = 0;

        this.isDisconnected = true;
    }
   
    private String getPasswordKeyAndEnc(String username, String password)
    {
        Mysql db = new Mysql();
        byte[] key = db.getKeyByUsername(username);

        return Cypher.encrypt(password, key);
    }
}