/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import api.LobbyListener;
import api.PlayerReadyEmitter;
import api.PlayerReadyListener;
import api.models.LobbyRoom;

/**
 *
 * @author hannes
 */
public class LobbyStarter implements PlayerReadyListener {

    private final LobbyHolder lobbyHolder;
    public LobbyStarter(LobbyHolder lobbyHolder) {
        this.lobbyHolder = lobbyHolder;
    }
    
    

    @Override
    public void notifyPlayerReady(int playerID, int roomID) {
        boolean start = lobbyHolder.getLobbyRoom(roomID).setPlayerReady(playerID);
        if (start) {
            //TODO: start game
        }
    }
    
}
