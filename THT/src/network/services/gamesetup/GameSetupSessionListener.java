/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.Player;
import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author hannes
 */
public interface GameSetupSessionListener {
    @Asynchronous
    void initPlayer(Player p);
    
    @Asynchronous
    void startGame();
}
