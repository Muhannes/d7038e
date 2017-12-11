/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.util.ConnectionAttribute;

/**
 *
 * @author truls
 */
@Serializable
public class LobbyRoom{
    
    private final List<HostedConnection> players = new ArrayList<>();
    private final Map<Integer, Boolean> playersReady = new HashMap<>();
    private final int roomID;
    private final String roomName;
    private static int idCounter = 0;
    private static final int MAX_PLAYERS = 10;

    public LobbyRoom() {
        roomID = idCounter;
        roomName = "Lobby" + roomID;
        idCounter++;
    }
    
    public LobbyRoom(String name){
        roomID = idCounter;
        roomName = name;
        idCounter++;
    }
    
    public synchronized int getID(){
        return roomID;
    }
    
    public synchronized String getName(){
        return roomName;
    }
    
    public synchronized int getMaxPlayers(){
        return MAX_PLAYERS;
    }
    
    public synchronized int getNumPlayers(){
        return players.size();
    }
    
    public synchronized List<String> getPlayerNames(){
        List<String> names = new ArrayList<>();
        for (HostedConnection player : players) {
            names.add(player.getAttribute(ConnectionAttribute.NAME));
        }
        return names;
    }
    
    public synchronized boolean addPlayer(HostedConnection p){
        if (canJoin()) {
            players.add(p);
            playersReady.put(p.getId(), Boolean.FALSE);
            return true;
        }
        return false;
    }
    
    public synchronized boolean removePlayer(HostedConnection p){
        boolean removed = players.remove(p);
        if (removed) {
            playersReady.remove(p.getId());
        }
        return removed;
    }
    
    /**
     * sets the chosen player to ready,
     * returns true if all players in lobby is ready.
     * @param playerID
     * @return 
     */
    public synchronized boolean setPlayerReady(int playerID){
        playersReady.put(playerID, Boolean.TRUE);
        return !playersReady.containsValue(false);
    }
    
    public synchronized List<HostedConnection> getPlayers(){
        return players;
    }
    
    public synchronized List<Integer> getPlayerIDs(){
        List<Integer> ids = new ArrayList<>(Arrays.asList(
                playersReady.keySet().toArray(new Integer[playersReady.keySet().size()])));
        return ids;
    }
    
    public synchronized boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }

}
