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
public class LoginRegistrationUI
{
    private PacketListener packetListener;
    private PacketListener initListener;
    
    private enum Screen {LOGIN,REGISTER}
    private Screen currentScreen = Screen.LOGIN;
    
    private CardLayout layout = new CardLayout();
    private JPanel container = new JPanel(layout);

    private JTextField loginIdField = new JTextField();
    private JPasswordField loginPassField = new JPasswordField();

    private final JButton loginButton = new JButton("Login");
    private final JButton goRegisterButton = new JButton("Go to Registration");

    private JTextField registerIdField = new JTextField();
    private JPasswordField registerPassField = new JPasswordField();

    private final JButton registerButton = new JButton("Register");
    private final JButton goLoginButton = new JButton("Go to Login");

    private JLabel resultLogin = new JLabel("");
    private JLabel resultRegister = new JLabel("");
    
    Credentials creds;
    
    private String myUsername;
    
    LoginRegistrationUI(JFrame frame)
    {
        buildLoginPanel();
        buildRegisterPanel();
        
        frame.add(container);
        frame.setVisible(true);
        layout.show(container,"login");
    }
    
    private void buildLoginPanel()
    {
        JPanel p = new JPanel(new GridLayout(4,2));

        p.add(new JLabel("User ID: "));
        p.add(loginIdField);

        p.add(new JLabel("Password: "));
        p.add(loginPassField);

        p.add(loginButton);
        p.add(goRegisterButton);

        p.add(resultLogin);

        loginButton.addActionListener(e -> handleLogin());
        
        goRegisterButton.addActionListener(e ->
        {
                currentScreen = Screen.REGISTER;
                layout.show(container, "register");
        });
        container.add(p,"login");
    }

    private void buildRegisterPanel()
    {
        JPanel p = new JPanel(new GridLayout(4,2));

        p.add(new JLabel("User ID:"));
        p.add(registerIdField);

        p.add(new JLabel("Password:"));
        p.add(registerPassField);

        p.add(registerButton);
        p.add(goLoginButton);

        p.add(resultRegister);

        registerButton.addActionListener(e -> handleRegister());
       
        goLoginButton.addActionListener(e ->
        {
                currentScreen = Screen.LOGIN;
                layout.show(container, "login");
        });
        container.add(p,"register");
    }

    protected void handleLogin()
    {
        String id = loginIdField.getText();
        String pass = new String(loginPassField.getPassword());
        creds = new Credentials(true,id,pass);
        if (packetListener != null) packetListener.onPacket(creds.wrapCreds());
        this.myUsername = id;
        //resultLogin.setText("Logging in...");
    }

    private void handleRegister()
    {
        String id = registerIdField.getText();
        String pass = new String(registerPassField.getPassword());
        creds = new Credentials(false,id,pass);
        if (packetListener != null) packetListener.onPacket(creds.wrapCreds());

        //resultRegister.setText("Registering...");
       
    }
    
    private void askForHandShake()
    {
       // if (initListener != null) initListener.onPacket(ClientOpCodes.PUBLIC_KEY.name());
    }
    
    public void cleanupLogin()
    {   
        resultLogin.setText("You disconnected.");
        loginIdField.setText("");
        loginPassField.setText("");
    }
    
    public void cleanupReg()
    {
        registerIdField.setText("");
        registerPassField.setText("");
        resultRegister.setText("");
    }
    
    public void screenState(String systemMessage)
    {
            System.out.println("screenState: "+systemMessage+" currentScreen"+currentScreen);
        switch (currentScreen)
                   {
                       case LOGIN -> resultLogin.setText(systemMessage);
                       case REGISTER -> resultRegister.setText(systemMessage);
                       default -> {}
                   }
    }
    public void setPacketListener(PacketListener packets)
    {
        this.packetListener = packets;
    }
    public void setInitListener(PacketListener packets)
    {
        this.initListener = packets;
    }
    protected String getMyUsername()
    {
        return this.myUsername;
    }
}
