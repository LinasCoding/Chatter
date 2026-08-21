/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class Credentials
{
    boolean isLogin;
    String name;
    String password;
    
    Credentials(boolean isLog,String n,String p)
    {  
        this.isLogin = isLog;
        this.name = n;
        this.password = p;
    }
    String wrapCreds()
    {
         return (isLogin ? ClientOpCodes.LOGIN_REQUEST.name() : ClientOpCodes.REGISTER_REQUEST.name()) + ":" + name + ":" + password;
    }
}
