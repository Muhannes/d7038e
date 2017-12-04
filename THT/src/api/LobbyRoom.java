/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.util.List;

/**
 *
 * @author truls
 */
public interface LobbyRoom {
     
    boolean join(String id);
    
    void leave();
    
    List<Player> getPlayers();
    
}
