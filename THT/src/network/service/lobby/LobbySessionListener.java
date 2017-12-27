/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby;

import com.jme3.network.service.rmi.Asynchronous;
import java.util.List;

/**
 *
 * @author hannes
 */
public interface LobbySessionListener {
    
    @Asynchronous
    void updateLobby(List<LobbyRoom> rooms);
    
    @Asynchronous
    void playerJoinedLobby(String name);
    
    @Asynchronous
    void playerLeftLobby(String name);
    
    @Asynchronous
    void playerReady(String name, boolean ready);
    
    @Asynchronous
    void allReady(String ip, int port);
    
    @Asynchronous
    void joinedLobby(LobbyRoom room);
    
}
