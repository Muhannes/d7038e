/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import api.models.LobbyRoom;

/**
 *
 * @author truls
 */
public interface LobbyManager {
    
    LobbyRoom join(int roomid);

    void leave();
    
    void ready();
    
}
