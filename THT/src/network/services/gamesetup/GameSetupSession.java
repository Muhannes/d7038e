/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author hannes
 */
public interface GameSetupSession {
    @Asynchronous
    void join(int globalID, String key, String name);
    
    @Asynchronous
    void ready();
}
