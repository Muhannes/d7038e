/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import api.models.LobbyRoom;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hannes
 */
public class LobbyHolder{
    private final List<LobbyRoom> lobbyRooms = new ArrayList();

    public LobbyHolder() {
        addLobbyRoom(new LobbyRoom());//must be atleast one lobby room.
    }
    
    public synchronized final void addLobbyRoom(LobbyRoom lobbyRoom){
        // TODO: Check if ok here? so the check will be synchronized too?
        lobbyRooms.add(lobbyRoom);
    }
    
    public synchronized List<LobbyRoom> getRooms(){
        return lobbyRooms;
    }
    
    public synchronized LobbyRoom getLobbyRoom(int id){
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            if (lobbyRoom.getID() == id) {
                return lobbyRoom;
            }
        }
        return null;
    }
    
}
