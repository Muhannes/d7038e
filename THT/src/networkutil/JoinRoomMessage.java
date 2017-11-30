/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import api.models.LobbyRoom;

/**
 *
 * @author truls
 */
public class JoinRoomMessage extends AbstractTCPMessage {
     
    public LobbyRoom lobbyRoom;
    
    public JoinRoomMessage(){}
    
    public JoinRoomMessage(LobbyRoom lobbyRoom){
        this.lobbyRoom = lobbyRoom;
    }
}
