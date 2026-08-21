/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class Message
{
    private String receiver;
    private String messageToUser;
    
    Message(String receiver)
    {
        this.receiver = receiver;      
    }
    
    Message(String receiver,String message)
    {
        this.receiver = receiver;
        this.messageToUser = message;
    }
    
    String wrapMessage()
    {  
        return ClientOpCodes.SEND_MESSAGE.name()+":"+receiver+":"+messageToUser;
    }
    String wrapUpdate()
    {  
        return ClientOpCodes.GET_CHAT_HISTORY.name()+":"+receiver;
    }
    String wrapLastMsg()
    {
        return ClientOpCodes.GET_LAST_MESSAGE.name()+":"+receiver;
    } 
   
}
