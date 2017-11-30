/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

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
    
    public boolean removePlayer(int playerID){
        for (Player player : players) {
            if (player.getID() == playerID) {
                players.remove(player);
                return true;
            }
        }
        return false;
    }
    
    public List<Player> getPlayers(){
        return players;
    }
    
    public boolean canJoin(){
        return players.size() < MAX_PLAYERS;
    }
    
    public boolean isMember(int playerID){
        for (Player player : players) {
            if (player.getID() == playerID) {
                return true;
            }
        }
        return false;
    }
}
