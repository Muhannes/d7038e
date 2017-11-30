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
import api.models.Player;
import com.jme3.app.SimpleApplication;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class LobbyApplication extends SimpleApplication implements LobbyEmitter, LobbySelectionListener, PlayerConnectionEmitter, ConnectionListener{
    List<PlayerConnectionListener> playerConnectionListeners = new ArrayList<>();
    List<LobbyListener> lobbyListeners = new ArrayList<>();
    List<LobbyRoom> lobbyRooms = new ArrayList();
    List<Player> nonLobbyPlayers = new ArrayList<>();
    
    @Override
    public void simpleInitApp() {
        //TODO: all
        lobbyRooms.add(new LobbyRoom());//must be atleast one lobby room.
        
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        lobbyListeners.add(lobbyListener);
    }
    
    private void notifyLobbyListeners(LobbyRoom lobbyRoom){
        for (LobbyListener lobbyListener : lobbyListeners) {
            lobbyListener.notifyLobby(lobbyRoom);
        }
    }

    @Override
    public void notifyLobbySelection(LobbyRoom lobbyRoom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPlayerConnectionListener(PlayerConnectionListener playerConnectionListener) {
        playerConnectionListeners.add(playerConnectionListener);
    }
    
    /**
     * not the same as connectionAdded. 
     * This is for when a player connects to a lobby.
     * @param p 
     */
    private void notifyPlayerConnectionListener(Player p){
        for (PlayerConnectionListener playerConnectionListener : playerConnectionListeners) {
            playerConnectionListener.notifyPlayerConnection(p);
        }
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        //Create new Player object
        nonLobbyPlayers.add(new Player());
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            notifyLobbyListeners(lobbyRoom);
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
