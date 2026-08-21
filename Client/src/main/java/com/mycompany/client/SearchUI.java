/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.awt.*;
import javax.swing.*;


/**
 *
 * @author linas
 */
public class SearchUI
{
    
    private PacketListener packetListener;
    private PacketListener addListener;
    private Runnable backListener;
    
    private SearchEngine searchEngine;
    private RequestForFriend requestForFriend;
    
    private final JButton back = new JButton("Back");
    private final JButton search = new JButton("Search");
    
    private JLabel founduser = new JLabel();
    private final JButton adduser = new JButton("Add");
    private JPanel resultRow = new JPanel(new BorderLayout());
    private JTextField searchbar = new JTextField();
    
    private JPanel top;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel bottom;
    
    private String doesUsernameExist;
    
    SearchUI(JFrame frame)
    {
        searchVisuals(frame);
    }
    
    private void searchVisuals(JFrame frame)
    {
        frame.setLayout(new BorderLayout());

        founduser.setVisible(false);
        adduser.setVisible(false);

        top = new JPanel(new GridLayout(1,2));
        top.add(searchbar);
        top.add(search);

        frame.add(top, BorderLayout.NORTH);

        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(founduser);
    
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(adduser);

        resultRow.removeAll();
        resultRow.add(leftPanel, BorderLayout.WEST);
        resultRow.add(rightPanel, BorderLayout.EAST);

        frame.add(resultRow, BorderLayout.CENTER);
    
        bottom = new JPanel();
        bottom.add(back);

        frame.add(bottom, BorderLayout.SOUTH);

        search.addActionListener(e ->
        {
            this.doesUsernameExist = searchbar.getText().trim();
            
            founduser.setVisible(false);
            adduser.setVisible(false);

            resultRow.revalidate();
            resultRow.repaint();
            
            doesExist();
        });

        back.addActionListener(e ->
        {
            frame.dispose();
            if(backListener != null) backListener.run();         
        });
        
        adduser.addActionListener(e ->
        {
            String username = searchbar.getText().trim();
            requestForFriend = new RequestForFriend(username);
            founduser.setVisible(true);
            founduser.setText("Request sent.");
            adduser.setVisible(false);

            if(addListener != null) addListener.onPacket(requestForFriend.wrapRequest());   
        });
    }
    
   void doesUserExist(boolean showUI)
   {
        if(showUI)
        {
            founduser.setVisible(true);
            founduser.setText(doesUsernameExist);
            adduser.setVisible(true);
        }
        else
        {
            founduser.setVisible(true);
            founduser.setText("No username with such input.");
            adduser.setVisible(false);          
        }
        
        resultRow.revalidate();
        resultRow.repaint();  
    }
    
    public void doesExist()
    {
        searchEngine = new SearchEngine(doesUsernameExist);
        if (packetListener != null) packetListener.onPacket(searchEngine.wrapRequest());       
    }
    
    public void setPacketListener(PacketListener packet)
    {
        this.packetListener = packet;
    }
    public void setBackListener(Runnable listener)
    {
        this.backListener = listener;
    }
    public void askToBeFriend(PacketListener packet)
    {
        this.addListener = packet;
    }
}
