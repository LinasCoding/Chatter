/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class SearchEngine
{
    String username;
    
    SearchEngine(String username)
    {
        this.username = username;
    }
    
    String wrapRequest()
    {
        return ClientOpCodes.SEARCH_USER.name()+":"+username;
    }
    
}
