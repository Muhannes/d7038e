/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.service.login.Account;
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

    LobbyRoom() {
        roomID = idCounter;
        roomName = "Lobby" + roomID;
        idCounter++;
    }
    
    LobbyRoom(String name){
        roomID = idCounter;
        roomName = name;
        idCounter++;
    }
    
    synchronized int getID(){
        return roomID;
    }
    
    synchronized String getName(){
        return roomName;
    }
    
    synchronized int getMaxPlayers(){
        return MAX_PLAYERS;
    }
    
    synchronized int getNumPlayers(){
        return players.size();
    }
    
    synchronized List<String> getPlayerNames(){
        List<String> names = new ArrayList<>();
        for (HostedConnection player : players) {
            names.add(((Account)player.getAttribute(ConnectionAttribute.ACCOUNT)).name);
        }
        return names;
    }
    
    synchronized boolean addPlayer(HostedConnection p){
        if (canJoin()) {
            players.add(p);
            playersReady.put(p.getId(), Boolean.FALSE);
            return true;
        }
        return false;
    }
    
    synchronized boolean removePlayer(HostedConnection p){
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
    synchronized boolean setPlayerReady(int playerID){
        playersReady.put(playerID, Boolean.TRUE);
        return !playersReady.containsValue(false);
    }
    
    synchronized List<HostedConnection> getPlayers(){
        return players;
    }
    
    synchronized List<Integer> getPlayerIDs(){
        List<Integer> ids = new ArrayList<>();
        for(HostedConnection p : players){
            ids.add(((Account)p.getAttribute(ConnectionAttribute.ACCOUNT)).id);
        }
        return ids;
    }
    
    synchronized boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }

}
