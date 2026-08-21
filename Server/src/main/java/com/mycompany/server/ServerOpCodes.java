/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.server;

/**
 *
 * @author linas
 */
public enum ServerOpCodes
{
    HANDSHAKE,
    
    LOGIN_OK,
    LOGIN_FAIL,
    TOO_MANY_LOG_ATTEMPTS,
    ALREADY_LOGGED,
    USER_NOT_EXIST,
    DISCONNECTED,

    REGISTER_OK,
    REGISTER_FAIL,
    TOO_MANY_REG_ATTEMPTS,
    NO_NUMBER_AT_START,
    USER_ALREADY_EXISTS,

    USER_FOUND,
    USER_NOT_FOUND,
    SEARCH_RESULT,

    FRIEND_LIST,
    NO_FRIENDS,
    PENDING_LIST,
    NO_PENDINGS,
    
    FRIEND_REQUEST_SENT,
    FRIEND_REQUEST_ACCEPTED,
    FRIEND_REQUEST_DECLINED,
    REQUEST_RECEIVED,

    CHAT_HISTORY,
    FILES_HISTORY,
    NO_MESSAGES,
    NO_FILES,
    MESSAGE_SENT,
    LAST_MESSAGE,
    FILE_SENT,
    LAST_FILE,
    DOWNLOADED,
    ACTIVE_FAIL,
    IM_ACTIVE,
    OFFLINE,

    ERROR,
    INVALID_FORMAT,
    UNKNOWN_REQUEST
}
