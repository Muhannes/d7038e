/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import api.LobbyEmitter;
import api.LobbyListener;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class LobbyRoom implements LobbyEmitter{
    private final List<LobbyListener> lobbyListeners  = new ArrayList<>();
    
    private List<Player> players = new ArrayList<>();
    private int roomID;
    private static int idCounter = 0;
    private static final int MAX_PLAYERS = 10;

    public LobbyRoom() {
        roomID = idCounter;
        idCounter++;
    }
    
    public int getID(){
        return roomID;
    }
    
    public boolean addPlayer(Player p){
        if (canJoin()) {
            players.add(p);
            return true;
        }
        return false;
    }
    
    public Player removePlayer(int playerID){
        Player p = getPlayer(playerID);
        if (p != null) {
            p.setReady(false);
            players.remove(p);
        }
        return p;
    }
    
    /**
     * sets the chosen player to ready,
     * returns true if all players in lobby is ready.
     * @param playerID
     * @return 
     */
    public boolean setPlayerReady(int playerID){
        Player p = getPlayer(playerID);
        p.setReady(true);
        for (Player player : players) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }
    
    public List<Player> getPlayers(){
        return players;
    }
    
    public boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }
    
    public Player getPlayer(int playerID){
        for (Player player : players) {
            if (player.getID() == playerID) {
                return player;
            }
        }
        return null;
    }
    
    public void clearRoom(){
        lobbyListeners.clear();
        players.clear();
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        lobbyListeners.add(lobbyListener);
    }
    
    private void notifyLobbyListeners(){
        for (LobbyListener lobbyListener : lobbyListeners) {
            lobbyListener.notifyLobby(this);
        }
    }
}
