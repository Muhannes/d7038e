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
    void notifyGameOver();
    
    @Asynchronous
    void notifyPlayersKilled(List <String> victims, List <String> killers);
    
    @Asynchronous
    void notifyMonkeysCaught(List <String> catchers, List<String> monkeys);
    
    @Asynchronous
    void notifyTrapsPlaced(List <String> trapNames, List <Vector3f> newTraps);
    
    @Asynchronous
    void notifyTrapsTriggered(List <String> names, List <String> trapNames);    
}
