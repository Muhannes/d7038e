/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.lobby;

/**
 *
 * @author truls
 */
public interface LobbyGUIListener {
    
    void onQuitGame();
    
    void onJoinLobby(String lobbyName);
    
    void onRefresh();
    
    void onCreateLobby(String lobbyName);
}
