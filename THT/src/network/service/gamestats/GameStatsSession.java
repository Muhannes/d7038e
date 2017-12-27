/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import com.jme3.math.Vector3f;
import java.util.List;

/**
 *
 * @author hannes
 */
public interface GameStatsSession {
    
    void notifyPlayerKilled(String victim, String killer);
    
    void notifyPlayerEscaped(String name);
    
    void notifyTrapPlaced(String trapName, Vector3f newTrap);
    
    void notifyTrapTriggered(String name, String trapName);
}
