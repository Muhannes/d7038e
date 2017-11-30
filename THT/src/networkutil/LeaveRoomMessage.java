/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import api.models.LobbyRoom;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author truls
 */
@Serializable
public class LeaveRoomMessage extends AbstractTCPMessage{
    
    
    public LobbyRoom lobbyRoom;
    
    public LeaveRoomMessage(){}
    
    
    public LeaveRoomMessage(LobbyRoom lobbyRoom){
        this.lobbyRoom = lobbyRoom;
    }
}
