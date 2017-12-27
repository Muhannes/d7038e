/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby;

import java.util.List;

/**
 *
 * @author truls
 */
public interface LobbyRoom {
    
    /**
     * Returns a list with the names of players in the lobby
     * @return 
     */
    List<String> getPlayers();
    
    /**
     * Returns the name of the lobby
     * @return 
     */
    String getName();
    
    /**
     * Return the number of players in the lobby
     * @return 
     */
    int numberOfPlayers();
    
    /**
     * Return the maximum allowed players in this lobby
     * @return 
     */
    int maxPlayers();
    
    /**
     * Return the chat ID for this lobby
     * @return 
     */
    int getChatId();
}
