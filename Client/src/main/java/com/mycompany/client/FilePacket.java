/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

/**
 *
 * @author linas
 */
public class FilePacket
{
    private String receiver;
    private String fileName;
    private String data;

    protected FilePacket(String receiver)
    {
        this.receiver = receiver;
    }
    
    protected FilePacket(String receiver, String fileName, String data)
    {
        this.receiver = receiver;
        this.fileName = fileName;
        this.data = data;
    }

    protected String wrapFile()
    {
        return ClientOpCodes.SEND_FILE.name()+ ":" + receiver + ":" + fileName + ":" + data;
    } 
    protected String wrapUpdate()
    {  
        return ClientOpCodes.GET_FILES_HISTORY.name()+":"+receiver;
    }
}
