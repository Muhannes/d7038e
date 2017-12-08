/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.Player;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class PlayerInitEvent extends Event{
    Player player;

    public PlayerInitEvent(Player player) {
        this.player = player;
    }
    
}
