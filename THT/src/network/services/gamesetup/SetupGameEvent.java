/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import java.util.List;
import java.util.Map;
import network.services.lobby.ClientLobbyListener;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class SetupGameEvent extends Event{
    private Map<Integer, String> lobbyPlayers;
    private List<ClientLobbyListener> callbacks;

    public SetupGameEvent(Map<Integer, String> lobbyPlayers, List<ClientLobbyListener> callbacks) {
        this.lobbyPlayers = lobbyPlayers;
        this.callbacks = callbacks;
    }
    
    public Map<Integer, String> getPlayers(){
        return lobbyPlayers;
    }
    
    public List<ClientLobbyListener> getCallbacks(){
        return callbacks;
    }
    
}
