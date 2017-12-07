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
public interface LobbyManager {
    
    List<String> join(int roomid);

    @Asynchronous
    void leave();
    
    @Asynchronous
    void ready();
    
    boolean createLobby(String lobbyName);
    
    Map<String, Integer> getAllRooms();

    public boolean removeLobby(String lobbyName);
    
}
