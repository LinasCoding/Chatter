/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.nio.file.Files;
import java.util.Base64;


/**
 *
 * @author linas
 */
public class ChatUI
{   
   
    private PacketListener packetListener;  
    
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    
    private String friendName;
    private String myUsername;
    
    private java.util.Map<String, File> fileMap = new java.util.HashMap<>();
    private java.util.List<ChatEntry> buffer = new java.util.ArrayList<>();
    
    ChatUI(JFrame frame,String friendName,String myUsername)
    {
        this.friendName = friendName;
        this.myUsername = myUsername;
        chatVisuals(frame,friendName);
      
        rebuildFileMap();
    }
    
    public void chatVisuals(JFrame frame, String friendName)
    {   
        frame.setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setDragEnabled(true);
        
        dragAndDrop();
        mouseListener();
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Send");

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setPreferredSize(new Dimension(500, 150));

        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e ->
        {
            String message = messageField.getText().trim();

            if (!message.isEmpty())
            {
                messageArea.append("Me: " + message + "\n");
                messageField.setText("");
            
                Message msg = new Message(this.friendName,message);
            
                if(packetListener != null) packetListener.onPacket(msg.wrapMessage());        
            }
        });
        
        messageArea.append("Chat with " + friendName + "\n");
    }
    
    void getMessages(String msg)
    {
        String data = msg;

        if(data.startsWith("CHAT_HISTORY:")) data = data.substring("CHAT_HISTORY:".length());
        
        String[] parts = data.split("\\|\\|");

        for(String p : parts)
        {
            String[] s = p.split("::");

            if(s.length == 3)
            {
                ChatEntry e = new ChatEntry();
                e.sender = s[0];
                e.text = s[1];
                e.timestamp = Long.parseLong(s[2]);
                e.isFile = false;

                buffer.add(e);
            }
        }
        renderChat();
    }
    
    void getFiles(String msg)
    {     
        String data = msg;

        if(data.startsWith("FILES_HISTORY:")) data = data.substring("FILES_HISTORY:".length());
        
        String[] parts = data.split("\\|\\|");

        for (String part : parts)
        {
            String[] fields = part.split("::");

            if (fields.length < 4)
            {
                System.out.println("BAD FILE FORMAT: " + part);
                continue;
            }

            long timestamp;

            try
            {
                timestamp = Long.parseLong(fields[3]);
                
            } catch (Exception e) {
                continue;
            }

            ChatEntry e = new ChatEntry();
            e.sender = fields[0];
            e.text = fields[2];
            e.timestamp = timestamp;
            e.isFile = true;

            e.file_id = fields[1];

            buffer.add(e);
        }
        renderChat();
    }
    
    void receiveLastMessage(String msg)
    {   
        String data = msg.substring(13); 

        String[] sender = data.split("::");
        
        if(sender.length != 3)
        {
            System.out.println("BAD MESSAGE");
            return;
        }
        String from = sender[0];
        String text = sender[1];
        
        System.out.println("Message to friend: " + friendName);
        
         if (!from.equals(friendName)) return;
        
        messageArea.append(friendName + ": " + text + "\n");     
        messageArea.setCaretPosition(messageArea.getDocument().getLength());     
    }
    
    void receiveLastFile(String msg)
    {
        try
        {
            String data = msg.substring("LAST_FILE:".length());

            String[] parts = data.split("::");

            if(parts.length < 4)
            {
                System.out.println("BAD LAST_FILE PACKET");
                return;
            }

            String sender = parts[0];
            String fileId = parts[1];
            String fileName = parts[2];

            if (!sender.equals(friendName)) return;

            messageArea.append(sender + ": [FILE] " + fileId + " " + fileName + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
           
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void rebuildFileMap()
    {
        File folder = new File("downloads");
        if(!folder.exists()) return;

        File[] files = folder.listFiles();
        if(files == null) return;

        for(File f : files)
        {
            fileMap.put(f.getName(),f);
        }
    }
   
    void dragAndDrop()
    {
        messageArea.setTransferHandler(new TransferHandler()
        {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support)
            {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support)
            {
                try
                {
                    List<File> files = (List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    File file = files.get(0);
                    sendFile(file); 

                    return true;

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
    
    void mouseListener()
    {
        messageArea.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                try
                {
                    int offset = messageArea.viewToModel2D(e.getPoint());
                    int line = messageArea.getDocument().getDefaultRootElement().getElementIndex(offset);

                    String[] lines = messageArea.getText().split("\n");
                    if(line < 0 || line >= lines.length) return;

                    String text = lines[line];

                    if(text.contains("[OPEN]") || text.contains("[FILE]"))
                    {
                        String[] parts = text.split(" ");

                        if(parts.length < 4) return;

                        String fileId = parts[2];
                        String fileName = parts[3];

                        File file = resolveFile(fileId, fileName);

                        if(file == null)
                        {
                            if(packetListener != null)
                            {
                                packetListener.onPacket("DOWNLOAD_FILE:" + fileId);
                            }
                            return;
                        }
    
                        if(file != null && Desktop.isDesktopSupported())
                        {
                            Desktop.getDesktop().open(file);
                        }
                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void sendFile(File file)
    {
        try
        {
            byte[] data = Files.readAllBytes(file.toPath());

            String encoded = Base64.getEncoder().encodeToString(data);

            FilePacket packet = new FilePacket(friendName,file.getName(),encoded);

            if(packetListener != null) packetListener.onPacket(packet.wrapFile());

            messageArea.append("Me: [FILE] " + file.getName() + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());

        }catch(Exception e){
            e.printStackTrace();
            messageArea.append("File send failed\n");
        }
    }
    void downloadFile(String msg)
    {
        try
        {
            String data = msg.substring("DOWNLOADED:".length());

            String[] parts = data.split(":", 2);

            String fileId = parts[0];
            String base64 = parts[1];

            byte[] bytes = Base64.getDecoder().decode(base64);

            String fileName = null;

            for(ChatEntry e : buffer)
            {
                if(e.isFile && e.file_id.equals(fileId))
                {
                    fileName = e.text;
                    break;
                }
            }

            if(fileName == null)
            {
                System.out.println("Unknown file name for id: " + fileId);
                return;
            }

            File file = new File("downloads", fileName);
            file.getParentFile().mkdirs();

            Files.write(file.toPath(), bytes);

            fileMap.put(fileId, file);

            System.out.println("Downloaded: " + fileName);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private File resolveFile(String fileId, String fileName)
    {
        File file = fileMap.get(fileId);
        if(file != null && file.exists())
            return file;

        File diskFile = new File("downloads/" + fileName);

        if(diskFile.exists())
        {
            fileMap.put(fileId, diskFile);
            return diskFile;
        }
        return null;
    }
    
    private static class ChatEntry
    {
        String sender;
        String text;
        boolean isFile;
        long timestamp;
        String file_id;
    }

    private void renderChat()
    {
        messageArea.setText("");

        buffer.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

        for(ChatEntry e : buffer)
        {
            if(e.isFile)
            {
                if(e.sender.equals(myUsername))
                {
                    messageArea.append("ME" + ": [OPEN] " + e.file_id + " " + e.text + "\n");
                }
                else
                {
                    messageArea.append(e.sender + ": [OPEN] " + e.file_id + " " + e.text + "\n");
                }                  
            }
            else
            {
                if(e.sender.equals(myUsername))
                {
                    messageArea.append("ME" + ": " + e.text + "\n");
                }
                else
                {
                    messageArea.append(e.sender + ": " + e.text + "\n");
                }               
            }
        }
    } 
 
    public void loadFriend(String friendName)
    {
        this.friendName = friendName;

        resetChat();
        
        messageArea.setText("");
        messageArea.append("Chat with " + friendName + "\n");
    }
    
    void resetChat()
    {
        buffer.clear();
        messageArea.setText("Chat with " + friendName + "\n");
    }
    
    public void setSendMessage(PacketListener packets)
    {
       this.packetListener = packets;
    }
    
}
