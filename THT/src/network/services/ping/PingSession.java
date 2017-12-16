/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.ping;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface PingSession {
    
    /**
     * A client will call back on this method 
     * which will let the server calculate the ping
     */
    @Asynchronous
    void reply(); 
    
}
