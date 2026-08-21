/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.client;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 *
 * @author linas
 */
public class Security
{

    protected static byte[] keyGenerator()
    {
        SecureRandom secureRandom = new SecureRandom();

        byte[] bkey = new byte[32];
        secureRandom.nextBytes(bkey);
        //String key = Base64.getEncoder().encodeToString(bkey);
        return bkey;
    }
    
    private static byte[] xor(byte[] input, byte[] key)
    {
        byte[] out = new byte[input.length];

        for (int i = 0; i < input.length; i++)
        {
            out[i] = (byte)(input[i] ^ key[i % key.length]);
        }

        return out;
    }

    protected static String encrypt(String data, byte[] key)
    {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = xor(input, key);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    protected static String decrypt(String data, byte[] key)
    {
        byte[] input = Base64.getDecoder().decode(data);

        byte[] decrypted = xor(input, key);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    protected static String encryptData(byte[] sessionKeyAsData, String rsaKey) throws Exception
    {
        String[] parts = rsaKey.split(":");
        if(parts.length !=2)
        {
            System.out.println("Wrong RSA key came.");
        }
        String base64Key = parts[1];

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = rsa.doFinal(sessionKeyAsData);

        return ClientOpCodes.SEND_KEY.name() + ":" + Base64.getEncoder().encodeToString(encrypted);
    }
  
}
