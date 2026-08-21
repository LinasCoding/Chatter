/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
/**
 *
 * @author linas
 */
public class Protocol
{
    private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    
    private static final String IP = "127.0.0.1";
    private static final int PORT = 5000;
    private static final int BACKLOG = 10;

    private static final int THREAD_POOL_SIZE = 10;
    
    private static Mysql db = new Mysql();

    protected static void initServer()
    {
        db.resetAllUsersOffline();
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT,BACKLOG,InetAddress.getByName(IP)))
        {
            System.out.println("Server started on " + IP + ":" + PORT);

            while (!pool.isShutdown())
            {
                try
                {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                    //clientSocket.setSoTimeout(30000);

                    ClientHandler handler = new ClientHandler(clientSocket);   
                    pool.execute(handler);

                }catch(IOException e){
                    System.out.println("Error accepting client: " + e.getMessage());
                }
            }
        }catch(IOException e){
            System.out.println("Server error: " + e.getMessage());
        }finally{
            shutdownPool(pool);
        }
    }
   
    protected static void sendMessage(String from,String to,String msg)
    {
        ClientHandler receiver = clients.get(to);
        if(receiver != null)
        {
            receiver.sendSystemMessage(msg);
        }
    }
    
    protected static void sendFile(String from, String to,String meta)
    {
        
        ClientHandler receiver = clients.get(to);

        if (receiver != null)
        {
            receiver.sendSystemMessage(meta);
        } 
    }
   
    protected static void registerClient(String username,ClientHandler handler)
    {
        clients.put(username,handler);
    }

    protected static void removeClient(String username)
    {
        clients.remove(username);
    }
    
    private static void shutdownPool(ExecutorService pool)
    {
        System.out.println("Shutting down thread pool...");
        pool.shutdown();
    }
}
