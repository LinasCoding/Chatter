/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class RequestForFriend
{
    private String newFriendUsername; 
   
    RequestForFriend(String request)
    {
        this.newFriendUsername = request;
    }
    
    String wrapRequest()
    {  
        return ClientOpCodes.SEND_FRIEND_REQUEST.name()+":"+newFriendUsername;
    }
   
}
