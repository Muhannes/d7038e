/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import com.jme3.math.Vector3f;
import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author hannes
 */
public interface GameStatsSession {
    @Asynchronous
    void notifyTrapPlaced(String trapName, Vector3f newTrap);
    
    @Asynchronous
    void notifyJump(String player);
    
    @Asynchronous
    void notifySlash(String player);
}
