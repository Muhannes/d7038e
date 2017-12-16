/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.ping;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface PingSessionListener {
    
    /**
     * This is called by the server to notify the ping to 
     * the client.
     * @param ms Ping in miliseconds
     */
    @Asynchronous
    void notifyPing(int ms);
    
}
