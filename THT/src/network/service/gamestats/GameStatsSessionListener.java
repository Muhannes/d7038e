/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import com.jme3.math.Vector3f;

/**
 *
 * @author hannes
 */
public interface GameStatsSessionListener {
    
    void notifyPlayerKilled(String victim, String killer);
    
    void notifyPlayerEscaped(String name);
    
    void notifyTrapPlaced(String id, Vector3f newTrap);
    
    void notifyTrapTriggered(String id);

}
