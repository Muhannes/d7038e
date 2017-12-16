/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hannes
 */
public class LobbyHolder{
    private final List<LobbyRoom> lobbyRooms = new ArrayList();

    public LobbyHolder() {
        //addLobbyRoom(new LobbyRoom());//must be atleast one lobby room.
    }
    
    public synchronized final boolean addLobbyRoom(LobbyRoom lobbyRoom){
        // TODO: Check if ok here? so the check will be synchronized too?
        lobbyRooms.add(lobbyRoom);
        return true;
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
    
    public synchronized final boolean removeLobbyRoom(String RoomName){
        for(LobbyRoom lr : lobbyRooms){
            lobbyRooms.remove(lr);
            return true;
        }
        return false;
    }
    
}
