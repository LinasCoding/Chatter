/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.server;

/**
 *
 * @author linas
 */
public enum ClientOpCodes
{
    SEND_KEY,
    
    LOGIN_REQUEST,
    REGISTER_REQUEST,
    DISCONNECT_REQUEST,

    GET_FRIEND_LIST,
    GET_PENDING_LIST,
    SEARCH_USER,

    SEND_FRIEND_REQUEST,
    ACCEPT_FRIEND_REQUEST,
    DECLINE_FRIEND_REQUEST,

    GET_CHAT_HISTORY,
    GET_FILES_HISTORY,
    SEND_MESSAGE,
    SEND_FILE,
    GET_LAST_MESSAGE,
    DOWNLOAD_FILE,
    
    SEND_IM_ACTIVE,
}
