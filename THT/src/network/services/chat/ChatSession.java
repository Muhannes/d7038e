/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.chat;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface ChatSession {
    
    @Asynchronous
    void sendMessage(String message);
    
}
