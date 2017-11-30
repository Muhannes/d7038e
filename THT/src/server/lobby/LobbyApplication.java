/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import api.LobbyEmitter;
import api.LobbyListener;
import api.LobbySelectionListener;
import api.PlayerConnectionEmitter;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import com.jme3.app.SimpleApplication;

/**
 *
 * @author truls
 */
public class LobbyApplication extends SimpleApplication implements LobbyEmitter, LobbySelectionListener, PlayerConnectionEmitter{

    @Override
    public void simpleInitApp() {
        //TODO: all
        
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyLobbySelection(LobbyRoom lobbyRoom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPlayerConnectionListener(PlayerConnectionListener playerConnectionListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
