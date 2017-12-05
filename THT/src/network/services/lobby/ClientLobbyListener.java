/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

/**
 *
 * @author hannes
 */
public interface ClientLobbyListener {
    void updateLobby(String lobbyName, int numPlayers, int maxPlayers);
    
    void playerJoined(String name);
    
    void playerLeft(String name);
    
    void playerReady(String name, boolean ready);
    
}
