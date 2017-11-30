/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby.network;

import api.LobbySelectionEmitter;
import api.LobbySelectionListener;
import api.models.LobbyRoom;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.List;
import networkutil.JoinRoomMessage;
import networkutil.LeaveRoomMessage;

/**
 *
 * @author hannes
 */
class LobbyMessageListener implements MessageListener<HostedConnection>, LobbySelectionEmitter {
    private final List<LobbySelectionListener> lobbySelectionListeners = new ArrayList<>();

    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof JoinRoomMessage) {
            onJoinRoomMessage((JoinRoomMessage) m, source);
            
        } else if (m instanceof LeaveRoomMessage) {
            onLeaveRoomMessage((LeaveRoomMessage) m, source);
            
        }
    }
    
    private void onJoinRoomMessage(JoinRoomMessage m, HostedConnection source){
        int playerID = source.getId();
        if (m.lobbyRoom != null) {
            notifyLobbySelectionListeners(m.lobbyRoom, playerID);
        }
        
    }
    
    private void onLeaveRoomMessage(LeaveRoomMessage m, HostedConnection source){
        
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
    
}
