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
public interface GameStatsSession {
    
    @Asynchronous
    void notifyPlayerKilled(String victim, String killer);
    
    @Asynchronous
    void notifyPlayerEscaped(String name);
    
    @Asynchronous
    void notifyTrapPlaced(String trapName, Vector3f newTrap);
    
    @Asynchronous
    void notifyTrapTriggered(String name, String trapName);

    @Asynchronous
    void notifyTrapsTriggered(List<String> names, List<String> trapNames);
}
