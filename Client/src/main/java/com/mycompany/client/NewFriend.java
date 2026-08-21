/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class NewFriend
{
    String newFriendUsername;
    
    NewFriend(String username)
    {
        this.newFriendUsername = username;
    }
    
    public String acceptFriendship()
    {
        return ClientOpCodes.ACCEPT_FRIEND_REQUEST.name()+":"+newFriendUsername;
    }
    public String declineFriendship()
    {
        return ClientOpCodes.DECLINE_FRIEND_REQUEST+":"+newFriendUsername;
    }
    
}
