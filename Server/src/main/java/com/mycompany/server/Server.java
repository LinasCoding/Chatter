/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.server;

/**
 *
 * @author linas
 */
public class Server
{  
    public static void main(String[] args)
    {
        CreateMysqlTables.initDatabaseTable();
        
        Protocol.initServer();
    } 
}
