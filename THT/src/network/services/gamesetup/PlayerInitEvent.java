/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.Player;
import java.util.List;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class PlayerInitEvent extends Event{
    public List<Player> players;

    public PlayerInitEvent(List<Player> players) {
        this.players = players;
    }
    
}
