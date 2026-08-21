/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.server;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
/**
 *
 * @author linas
 */
public class Cypher
{   
    protected static byte[] keyGeneratorForPsw()
    {
        SecureRandom secureRandom = new SecureRandom();

        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return key;
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

    protected static String decrypt(String data, byte[] key)
    {
        byte[] input = Base64.getDecoder().decode(data);

        byte[] decrypted = xor(input, key);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    protected static String encrypt(String data, byte[] key)
    {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = xor(input, key);

        return Base64.getEncoder().encodeToString(encrypted);
    }
}
