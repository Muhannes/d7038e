/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.chat;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface ChatSession {
    
    /**
     * Authenticate the client to the chat server
     * @param id Client's global id
     * @param key Client's key to verify itself
     * @param name Client's name
     */
    @Asynchronous
    void authenticate(int id, String key, String name);
    
    /**
     * Sends a message to the chat identified by 
     * @param message Message
     * @param chat Chat ID
     */
    @Asynchronous
    void sendMessage(String message, int chat);
    
    /**
     * Join the chat with the given id
     * @param chat Id of the chat to join
     */
    @Asynchronous
    void joinchat(int chat);
    
    /**
     * Leave the chat with the given id
     * @param chat Id of the chat to leave
     */
    @Asynchronous
    void leavechat(int chat);
    
}
