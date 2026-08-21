/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.io.*;
import java.net.*;

/**
 *
 * @author linas
 */

public class Protocol
{

    private final String ip = "127.0.0.1";
    private final int port = 5000;

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    private volatile MessageListener listener;
   

    public void connect() throws IOException
    {
        socket = new Socket(ip, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        startListening();
    }

    public void send(String msg)
    {
        System.out.println("FROM CLIENT'S Protocol -> "+msg);
        try
        {
            output.write(msg);
            output.newLine();
            output.flush();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void startListening()
    {
        new Thread(() ->
        {
            try
            {
                String msg;
                while ((msg=input.readLine()) != null)
                {
                    if(listener != null)
                    {
                        listener.onMessage(msg);
                    }
                }
            }catch(IOException e){
                System.out.println("Connection closed");
                
                if (listener != null)
                {
                    listener.onMessage("DISCONNECT_REQUEST");
                }
            }
        }).start();
    }

    public void close()
    {
        try
        {
            if (input != null) input.close();     
            if (output != null) output.close();   
            if(socket != null) socket.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setListener(MessageListener listener)
    {
        this.listener = listener;
    }
}