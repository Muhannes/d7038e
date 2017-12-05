/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import api.LobbyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author truls
 */
public class LobbyRoom{
    private final List<LobbyListener> lobbyListeners  = new ArrayList<>();
    
    private final List<PlayerImpl> players = new ArrayList<>();
    private final Map<Integer, Boolean> playersReady = new HashMap<>();
    private final int roomID;
    private static int idCounter = 0;
    private static final int MAX_PLAYERS = 10;

    public LobbyRoom() {
        roomID = idCounter;
        idCounter++;
    }
    
    public synchronized int getID(){
        return roomID;
    }
    
    public synchronized boolean addPlayer(PlayerImpl p){
        if (canJoin()) {
            players.add(p);
            playersReady.put(p.getID(), Boolean.FALSE);
            return true;
        }
        return false;
    }
    
    public synchronized PlayerImpl removePlayer(int playerID){
        PlayerImpl p = getPlayer(playerID);
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
    public synchronized boolean setPlayerReady(int playerID){
        playersReady.put(playerID, Boolean.TRUE);
        return playersReady.containsValue(false);
    }
    
    public synchronized List<PlayerImpl> getPlayers(){
        return players;
    }
    
    public synchronized boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }
    
    public synchronized PlayerImpl getPlayer(int playerID){
        for (PlayerImpl player : players) {
            if (player.getID() == playerID) {
                return player;
            }
        }
        return null;
    }
    
    public synchronized void clearRoom(){
        lobbyListeners.clear();
        players.clear();
    }

}
