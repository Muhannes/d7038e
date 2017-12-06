/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author hannes
 */
public interface ClientLobbyListener {
    @Asynchronous
    void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers);
    
    @Asynchronous
    void playerJoined(String name);
    
    @Asynchronous
    void playerLeft(String name);
    
    @Asynchronous
    void playerReady(String name, boolean ready);
    
}