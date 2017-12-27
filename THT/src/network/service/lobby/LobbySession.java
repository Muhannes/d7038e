/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author truls
 */
public interface LobbySession {
    
    /**
     * Authenticate with the lobby server
     * @param id
     * @param key 
     */
    @Asynchronous
    void authenticate(int id, String key);
    
    /**
     * Join the room with the given name
     * @param room Name of room
     */
    @Asynchronous
    void join(String room);

    /**
     * Leave the room 
     */
    @Asynchronous
    void leave();
    
    /**
     * Notify that you are ready to begin playing the game
     */
    @Asynchronous
    void ready();
    
    /**
     * Fetches all rooms
     */
    @Asynchronous
    void fetchAllRooms();
    
}
