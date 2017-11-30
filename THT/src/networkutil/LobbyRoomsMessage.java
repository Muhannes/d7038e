/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import api.models.LobbyRoom.SerializedLobbyRoom;
import com.jme3.network.serializing.Serializable;
import java.util.List;

/**
 *
 * @author hannes
 */
@Serializable
public class LobbyRoomsMessage extends AbstractTCPMessage {
    public List<SerializedLobbyRoom> lobbyRooms;
    
    public LobbyRoomsMessage(){
        
    }
    
    public LobbyRoomsMessage(List<SerializedLobbyRoom> lobbyRooms){
        this.lobbyRooms = lobbyRooms;
    }
    
}
