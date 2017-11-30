/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import api.models.LobbyRoom.SerializedLobbyRoom;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author hannes
 */
@Serializable
public class JoinRoomAckMessage extends AbstractTCPMessage {
    public SerializedLobbyRoom lobbyRoom;
    
    public JoinRoomAckMessage() {
    }
    
    public JoinRoomAckMessage(SerializedLobbyRoom lobbyRoom){
        this.lobbyRoom = lobbyRoom;
    }
}
