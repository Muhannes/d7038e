/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.gamelobby;

/**
 *
 * @author truls
 */
public interface GameLobbyGUIListener {
    
    void onReady();
    
    void onReturnToLobby();
    
    void onQuitGame();
    
    void onSendMessage(String message);
}
