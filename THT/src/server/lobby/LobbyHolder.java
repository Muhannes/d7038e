/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import api.LobbyEmitter;
import api.LobbyListener;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import api.models.Player;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hannes
 */
public class LobbyHolder implements LobbyEmitter, PlayerConnectionListener {
    
    private final List<LobbyListener> lobbyListeners = new ArrayList<>();
    private final List<LobbyRoom> lobbyRooms = new ArrayList();

    public LobbyHolder() {
        addLobbyRoom(new LobbyRoom());//must be atleast one lobby room.
    }
    
    public void addLobbyRoom(LobbyRoom lobbyRoom){
        // Maybe check possibility here?
        
        addLobbyListeners(lobbyRoom);
        lobbyRooms.add(lobbyRoom);
    }
    
    public List<LobbyRoom> getRooms(){
        return lobbyRooms;
    }
    
    public LobbyRoom getLobbyRoom(int id){
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            if (lobbyRoom.getID() == id) {
                return lobbyRoom;
            }
        }
        return null;
    }
    
    public Player getPlayer(int playerID, int roomID){
        return getLobbyRoom(roomID).getPlayer(playerID);
    }

    @Override
    public void notifyPlayerConnection(Player player, LobbyRoom lobbyRoom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        lobbyListeners.add(lobbyListener);
        for (LobbyRoom room : getRooms()) {
            addLobbyListeners(room);
        }
    }
    
    private void addLobbyListeners(LobbyRoom lobbyRoom){
        for (LobbyListener lobbyListener : lobbyListeners) {
            lobbyRoom.addLobbyListener(lobbyListener);
        }
    }
    
}
