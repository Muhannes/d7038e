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
public interface ChatSessionListener {
    
    /**
     * This method is inovoked when a new message is
     * received from the server
     * @param message Message
     * @param chat Id of the chat that the message was posted in
     */
    @Asynchronous
    void newMessage(String message, int chat);
    
    /**
     * This method is invoked when a new paricipant joins a chat
     * @param name Name of chat participant
     * @param chat Id of chat
     */
    @Asynchronous
    void playerJoinedChat(String name, int chat);
    
    /**
     * This method is invoked when a participant leaves a chat
     * @param name Name of chat participant
     * @param chat Id of chat
     */
    @Asynchronous
    void playerLeftChat(String name, int chat);
}
