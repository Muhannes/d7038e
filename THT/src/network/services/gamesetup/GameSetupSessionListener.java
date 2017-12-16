/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.Player;
import com.jme3.network.service.rmi.Asynchronous;
import java.util.List;

/**
 *
 * @author hannes
 */

public interface GameSetupSessionListener {
    
    @Asynchronous
    void initPlayer(List<Player> players);
    
    @Asynchronous
    void startGame();
    
}
