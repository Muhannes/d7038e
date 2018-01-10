/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import com.jme3.math.Vector3f;
import com.jme3.network.service.rmi.Asynchronous;
import java.util.List;

/**
 *
 * @author hannes
 */
public interface GameStatsSessionListener {
    
    @Asynchronous
    void notifyGameOver(String winners);
    
    @Asynchronous
    void notifyPlayersKilled(String victim, String killer);
    
    @Asynchronous
    void notifyMonkeysCaught(String catcher, String monkey);
    
    @Asynchronous
    void notifyTrapsPlaced(String trapName, Vector3f newTrap);
    
    @Asynchronous
    void notifyTrapsTriggered(String name, String trapName);    
    
    @Asynchronous
    void notifyPlayerJumped(String player);
    
    @Asynchronous
    void notifyPlayerSlashed(String player);
}
