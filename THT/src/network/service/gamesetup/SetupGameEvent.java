/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamesetup;

import java.util.List;
import java.util.Map;
import utils.eventbus.Event;
import network.service.lobby.LobbySessionListener;

/**
 *
 * @author hannes
 */
public class SetupGameEvent extends Event{
    private Map<Integer, String> lobbyPlayers;
    private List<LobbySessionListener> callbacks;

    public SetupGameEvent(Map<Integer, String> lobbyPlayers, List<LobbySessionListener> callbacks) {
        this.lobbyPlayers = lobbyPlayers;
        this.callbacks = callbacks;
    }
    
    public Map<Integer, String> getPlayers(){
        return lobbyPlayers;
    }
    
    public List<LobbySessionListener> getCallbacks(){
        return callbacks;
    }
    
}
