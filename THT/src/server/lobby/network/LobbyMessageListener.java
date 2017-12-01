/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby.network;

import api.LobbySelectionEmitter;
import api.LobbySelectionListener;
import api.PlayerReadyEmitter;
import api.PlayerReadyListener;
import api.models.LobbyRoom;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.List;
import networkutil.JoinRoomMessage;
import networkutil.LeaveRoomMessage;
import networkutil.ReadyMessage;

/**
 *
 * @author hannes
 */
class LobbyMessageListener implements MessageListener<HostedConnection>, LobbySelectionEmitter, PlayerReadyEmitter {
    private final List<LobbySelectionListener> lobbySelectionListeners = new ArrayList<>();
    private final List<PlayerReadyListener> playerReadyListeners = new ArrayList<>();
    
    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof JoinRoomMessage) {
            if ((int) source.getAttribute(LobbyNetworkStates.ROOM_ID) == -1) { // if not already in a room
                onChangeRoomMessage(((JoinRoomMessage) m).lobbyRoom, source);
            }
            
        } else if (m instanceof LeaveRoomMessage) {
            if ((int) source.getAttribute(LobbyNetworkStates.ROOM_ID) != -1) { // if in a room
                onChangeRoomMessage(((LeaveRoomMessage) m).lobbyRoom, source);
            }
            
        } else if (m instanceof ReadyMessage) {
            int roomID = (int) source.getAttribute(LobbyNetworkStates.ROOM_ID);
            if (roomID != -1) { // if in a room
                onReadyMessage(roomID);
            }
        }
    }
    
    private void onChangeRoomMessage(LobbyRoom lobbyRoom, HostedConnection source){
        int playerID = source.getId();
        if (lobbyRoom != null) {
            notifyLobbySelectionListeners(lobbyRoom, playerID);
        }
        
    }
    
    private void onReadyMessage(int roomID){
        
    }

    @Override
    public void addLobbySelectionListener(LobbySelectionListener lobbySelectionListener) {
        lobbySelectionListeners.add(lobbySelectionListener);
    }
    
    private void notifyLobbySelectionListeners(LobbyRoom lobbyRoom, int playerID){
        for (LobbySelectionListener lobbySelectionListener : lobbySelectionListeners) {
            lobbySelectionListener.notifyLobbySelection(lobbyRoom, playerID);
        }
    }

    @Override
    public void addPlayerReadyListener(PlayerReadyListener playerReadyListener) {
        playerReadyListeners.add(playerReadyListener);
    }
    
}
