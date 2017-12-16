/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import com.jme3.network.service.rmi.Asynchronous;
import java.util.List;
import java.util.Map;

/**
 *
 * @author truls
 */
public interface LobbySession {
    
    void authenticate(int id, String key);
    
    List<String> join(int roomid);

    @Asynchronous
    void leave();
    
    @Asynchronous
    void ready();
    
    int createLobby(String lobbyName);
    
    Map<String, Integer> getAllRooms();

    public boolean removeLobby(String lobbyName);
    
}
