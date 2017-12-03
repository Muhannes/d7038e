/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface ChatSessionListener {
    
    @Asynchronous
    void newMessage(String message);
}
