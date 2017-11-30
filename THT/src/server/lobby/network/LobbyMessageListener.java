/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby.network;

import api.models.LobbyRoom;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import networkutil.JoinRoomMessage;
import networkutil.LeaveRoomMessage;

/**
 *
 * @author hannes
 */
class LobbyMessageListener implements MessageListener<HostedConnection> {


    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof JoinRoomMessage) {
            onJoinRoomMessage((JoinRoomMessage) m, source);
            
        } else if (m instanceof LeaveRoomMessage) {
            onLeaveRoomMessage((LeaveRoomMessage) m, source);
            
        }
    }
    
    private void onJoinRoomMessage(JoinRoomMessage m, HostedConnection source){
        LobbyRoom lr = m.lobbyRoom;
        lr.getID();
    }
    
    private void onLeaveRoomMessage(LeaveRoomMessage m, HostedConnection source){
        
    }
    
}
