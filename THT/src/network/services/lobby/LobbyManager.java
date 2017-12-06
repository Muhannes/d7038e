/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import api.models.LobbyRoom;
import com.jme3.network.service.rmi.Asynchronous;
import java.util.Map;

/**
 *
 * @author truls
 */
public interface LobbyManager {
    
    LobbyRoom join(int roomid);

    @Asynchronous
    void leave();
    
    @Asynchronous
    void ready();
    
    LobbyRoom createLobby(String lobbyName);
    
    Map<String, Integer> getAllRooms();
    
}