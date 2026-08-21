/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.io.RandomAccessFile;
import java.nio.channels.*;
/**
 *
 * @author linas
 */
public class Client
{
    private static FileLock lock;
    private static FileChannel channel;

    public static void main(String[] args)
    {
//////////////////////////////////////////////////////////////////////////////////REMOVE BLOCK BELOW FOR TESTING///////////////////////////////
        try
        {
            channel = new RandomAccessFile("client.lock", "rw").getChannel();
            lock = channel.tryLock();

            if (lock == null)
            {
                System.out.println("Client is already running!");
                System.exit(0);
            }

            System.out.println("Client started");

            Runtime.getRuntime().addShutdownHook(new Thread(() ->
            {
                try
                {
                    if (lock != null) lock.release();
                    if (channel != null) channel.close();
                }
                catch(Exception ignored) {}
            }));

        }
        catch(Exception e)
        {
            System.out.println("Client already running or lock failed");
            System.exit(0);
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Graph login = new Graph();
        
    }
}
