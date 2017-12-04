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
import api.models.PlayerImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hannes
 */
public class LobbyHolder implements LobbyEmitter{
    
    private final List<LobbyListener> lobbyListeners = new ArrayList<>();
    private final List<LobbyRoom> lobbyRooms = new ArrayList();

    public LobbyHolder() {
        addLobbyRoom(new LobbyRoom());//must be atleast one lobby room.
    }
    
    public synchronized final void addLobbyRoom(LobbyRoom lobbyRoom){
        // TODO: Check if ok here? so the check will be synchronized too?
        lobbyRooms.add(lobbyRoom);
        notifyLobbyListeners(lobbyRoom);
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
    
    public PlayerImpl getPlayer(int playerID, int roomID){
        return getLobbyRoom(roomID).getPlayer(playerID);
    }
    
    public boolean addPlayer(PlayerImpl p, int roomID){
        LobbyRoom lr = getLobbyRoom(roomID);
        boolean ok =  lr.addPlayer(p);
        if (ok) {
            notifyLobbyListeners(lr);
        }
        return ok;
    }
    
    public PlayerImpl removePlayer(int playerID, int roomID){
        LobbyRoom lr = getLobbyRoom(roomID);
        PlayerImpl p =  lr.removePlayer(playerID);
        if (p != null) {
            notifyLobbyListeners(lr);
        }
        return p;
    }
    
    /**
     * sets a player in a room ready.
     * @param playerID
     * @param roomID
     * @return true if all players in room is ready
     */
    public boolean setPlayerReady(int playerID, int roomID){
        LobbyRoom lr = getLobbyRoom(roomID);
        boolean start =  lr.setPlayerReady(playerID);
        return start;
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        lobbyListeners.add(lobbyListener);
    }
    
    private void notifyLobbyListeners(LobbyRoom lobbyRoom){
        for (LobbyListener lobbyListener : lobbyListeners) {
            lobbyListener.notifyLobby(lobbyRoom);
        }
    }
}
