/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import java.util.Map;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class SetupGameEvent extends Event{
    private Map<Integer, String> lobbyPlayers;

    public SetupGameEvent(Map<Integer, String> lobbyPlayers) {
        this.lobbyPlayers = lobbyPlayers;
    }
    
    public Map<Integer, String> getPlayers(){
        return lobbyPlayers;
    }
    
    
}
