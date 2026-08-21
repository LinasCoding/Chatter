/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.util.function.Consumer;

/**
 *
 * @author linas
 */
public class MainUI
{
    
    private PacketListener packetListener;
    private Runnable loginListener;
    private Runnable searchListener;
    private Consumer<String> chatListener;
    private PacketListener updateListenerMsg;
    private PacketListener updateListenerFile;
    
    
    private final JButton search = new JButton("Search");
    private final JButton exit = new JButton("Exit to login");
    
    private List<JButton> buttonForFriends;
    
    private JPanel top;
    private JPanel bottom;
   
    private JPanel friendsPanel;   
    private JPanel pendingPanel;   

    private JTabbedPane tabs;      

    private NewFriend newFriend;
    
    
    MainUI(JFrame frame)
    {
        mainVisuals(frame);
    }
    
    private void mainVisuals(JFrame frame)
    {

        top = new JPanel(new BorderLayout());
        top.add(new JLabel("Friends List",SwingConstants.CENTER), BorderLayout.CENTER);
        top.add(search, BorderLayout.EAST);

        search.addActionListener(e ->
        {
            frame.dispose();
            if(searchListener != null)
            {
                searchListener.run();
            }
        });

        frame.add(top, BorderLayout.NORTH);

        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));

        pendingPanel = new JPanel();
        pendingPanel.setLayout(new BoxLayout(pendingPanel, BoxLayout.Y_AXIS));
        
        tabs = new JTabbedPane();
        tabs.add("Friends", new JScrollPane(friendsPanel)); 
        tabs.add("Requests", new JScrollPane(pendingPanel)); 
        
        frame.add(tabs, BorderLayout.CENTER); 

       
        buttonForFriends = new ArrayList<>();

        bottom = new JPanel();

        exit.addActionListener(e ->
        {
            if(packetListener != null) packetListener.onPacket(ClientOpCodes.DISCONNECT_REQUEST.name());
    
            frame.dispose();

            if(loginListener != null) loginListener.run();
           
        });

        bottom.add(exit);
        frame.add(bottom, BorderLayout.SOUTH);
    }
    
    public void showFriendList(String message)
    {
        String data = message.substring(12);

        List<String> friends = data.isEmpty() ? new ArrayList<>() : Arrays.asList(data.split(","));

        friendsPanel.removeAll();
        buttonForFriends.clear();
        
        for (String friend : friends)
        {
            String[] parts = friend.split("::");

            String username = parts[0];
            boolean isActive = Boolean.parseBoolean(parts[1]);

            JPanel row = createUserRow(username, false);
            friendsPanel.add(row);

            updateFriendStatus(username, isActive);
        }
        
        friendsPanel.revalidate();
        friendsPanel.repaint();
    }
    
    public void showFriendRequests(String message)
    {
        String data = message.substring(13);
        List<String> requests;

        if(data.isEmpty())
        {
            requests = new ArrayList<>();
            showEmpty(false);
        }
        else
        {
            requests = Arrays.asList(data.split(","));
        }

        pendingPanel.removeAll();
   
        for (String user : requests)
        {
            JPanel row = createUserRow(user, true); 
            
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            pendingPanel.add(row);
        }  

        pendingPanel.revalidate();
        pendingPanel.repaint();
    }
    
    private JPanel createUserRow(String username, boolean isRequest)
    {
        JPanel row = new JPanel(new BorderLayout());

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    
        if (!isRequest)
        {
            JButton button = new JButton(username);

            buttonForFriends.add(button);
            
            button.addActionListener(e ->
            {
                if (chatListener != null) chatListener.accept(username);
                Message message = new Message(username);
                if(updateListenerMsg!=null) updateListenerMsg.onPacket(message.wrapUpdate());
                FilePacket files = new FilePacket(username);
                if(updateListenerFile!=null) updateListenerFile.onPacket(files.wrapUpdate());
            });

            row.add(button, BorderLayout.CENTER);
        }
        else
        {
            JLabel nameLabel = new JLabel(username);
            JPanel actions = new JPanel();

            JButton accept = new JButton("ACCEPT");
            JButton decline = new JButton("DECLINE");

            actions.add(accept);
            actions.add(decline);

            row.add(nameLabel, BorderLayout.CENTER);
            row.add(actions, BorderLayout.EAST);

            accept.addActionListener(e ->
            {
                System.out.println("ACCEPT: " + username);

                pendingPanel.remove(row);
                pendingPanel.revalidate();
                pendingPanel.repaint();
                                
                newFriend = new NewFriend(username);
                
                if(packetListener != null) packetListener.onPacket(newFriend.acceptFriendship());
                getFriendsList();    
                getPendingList();
            });

            decline.addActionListener(e ->
            {
                newFriend = new NewFriend(username);
                if(packetListener != null) packetListener.onPacket(newFriend.declineFriendship());
                System.out.println("DECLINE: " + username);
                               
                pendingPanel.remove(row);
                pendingPanel.revalidate();
                pendingPanel.repaint();

                getPendingList();
                
            });
        }
        return row;
    }
    
    public void showEmpty(boolean isList)
    {
        if(isList)
        {
            friendsPanel.removeAll();
            
            JLabel empty = new JLabel("Empty List");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            friendsPanel.add(empty);
            friendsPanel.revalidate();
            friendsPanel.repaint();
        }
        else
        {
            pendingPanel.removeAll();
            
            JLabel empty = new JLabel("Empty List");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            pendingPanel.add(empty);
            pendingPanel.revalidate();
            pendingPanel.repaint();
        }
   
    }
    
    public void updateFriendStatus(String username, boolean isActive)
    {
         SwingUtilities.invokeLater(() ->
         {
            for (JButton btn : buttonForFriends)
            {
                if (btn.getText().equals(username))
                {
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);

                    btn.setBackground(isActive ? Color.GREEN : Color.RED);
                    break;
                }
            }
         });
    } 
    
    void online(String message)
    {
       String onlineFriend = message.substring(10);
       updateFriendStatus(onlineFriend,true);
    }
    void offline(String message)
    {
       String onlineFriend = message.substring(8);
       updateFriendStatus(onlineFriend,false);
    }
    
    public void getFriendsList()
    {
       if (packetListener != null) packetListener.onPacket(ClientOpCodes.GET_FRIEND_LIST.name());       
    }
    
    public void getPendingList()
    {
       if (packetListener != null) packetListener.onPacket(ClientOpCodes.GET_PENDING_LIST.name());       
    }
    
    public void addNewFriend(PacketListener packets)
    {
        this.packetListener = packets;
    }
    
    public void setPacketListener(PacketListener packets)
    {
        this.packetListener = packets;
    }
    
    public void setDisconnectListener(PacketListener packets)
    {
        this.packetListener = packets;
    }
    
    public void setSearchListener(Runnable listener)
    {
        this.searchListener = listener;
    }
    
    public void setLoginListener(Runnable listener)
    {
        this.loginListener = listener;
    }
    
    public void setChatListener(Consumer<String> listener)
    {
        this.chatListener = listener;
    }
   
    public void updateChatFrameMsg(PacketListener packets)
    {
        this.updateListenerMsg = packets;
    }
    public void updateChatFrameFile(PacketListener packets)
    {
        this.updateListenerFile = packets;
    }
     
}
