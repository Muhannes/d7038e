/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hannes
 */
public class LobbyHolder{
    private final List<LobbyRoom> lobbyRooms = new ArrayList();

    LobbyHolder() {}
    
    synchronized final LobbyRoom addLobbyRoom(String name){
        // TODO: Check if ok here? so the check will be synchronized too?
        for(LobbyRoom r : lobbyRooms){
            if(r.getName().equals(name)){
                return null;
            }
        }
        LobbyRoom room = new LobbyRoom(name);
        lobbyRooms.add(room);
        return room;
    }
    
    synchronized List<LobbyRoom> getRooms(){
        return lobbyRooms;
    }
    
    synchronized LobbyRoom getLobbyRoom(int id){
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            if (lobbyRoom.getID() == id) {
                return lobbyRoom;
            }
        }
        return null;
    }
    
    synchronized final boolean removeLobbyRoom(String RoomName){
        for(LobbyRoom lr : lobbyRooms){
            lobbyRooms.remove(lr);
            return true;
        }
        return false;
    }
    
}
