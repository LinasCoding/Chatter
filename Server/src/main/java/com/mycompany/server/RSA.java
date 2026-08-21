/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;

import java.security.*;
import javax.crypto.Cipher;
/**
 *
 * @author linas
 */
public class RSA 
{   
    protected static KeyPair rsaKey() throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048); // key size
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }
    
    protected static byte[] decryptKey(String data,KeyPair kp) throws Exception
    {  
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        
        byte[] decode = java.util.Base64.getDecoder().decode(data);            
        rsa.init(Cipher.DECRYPT_MODE, kp.getPrivate());
        return rsa.doFinal(decode);    
    }
    
}
