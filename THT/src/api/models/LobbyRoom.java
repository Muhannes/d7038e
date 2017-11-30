/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class LobbyRoom {
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
    
    public void addPlayer(Player p){
        players.add(p);
    }
    
    public List<Player> getPlayers(){
        return players;
    }
    
    public boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }
    
    public SerializedLobbyRoom toSerializable(){
        return new SerializedLobbyRoom(players, roomID);
    }
    
    @Serializable
    public class SerializedLobbyRoom{
        private int id;
        private String[] playerNames;
        
        private SerializedLobbyRoom(List<Player> players, int id){
            playerNames = new String[players.size()];
            for(int i = 0; i < players.size(); i++){
                playerNames[i] = players.get(i).getName();
            }
            this.id = id;
        }
        
    }
}
