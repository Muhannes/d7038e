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
public interface GameStatsSessionListener {
    
    void notifyPlayersKilled(List <String> victims, List <String> killers);
    
    void notifyPlayersEscaped(List <String> names);
    
    void notifyTrapsPlaced(List <String> trapNames, List <Vector3f> newTraps);
    
    void notifyTrapsTriggered(List <String> names, List <String> trapNames);
}
