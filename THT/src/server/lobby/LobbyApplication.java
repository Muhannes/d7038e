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
import server.lobby.network.NetworkHandler;

/**
 *
 * @author truls
 */
public class LobbyApplication extends SimpleApplication implements LobbyEmitter, LobbySelectionListener, PlayerConnectionEmitter, ConnectionListener{
    List<PlayerConnectionListener> playerConnectionListeners = new ArrayList<>();
    List<LobbyListener> lobbyListeners = new ArrayList<>();
    List<LobbyRoom> lobbyRooms = new ArrayList();
    List<Player> nonLobbyPlayers = new ArrayList<>();
    NetworkHandler networkHandler;

    public LobbyApplication(NetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }
    
    @Override
    public void simpleInitApp() {
        //TODO: all
        lobbyRooms.add(new LobbyRoom());//must be atleast one lobby room.
        
    }
    
    public synchronized LobbyRoom getLobbyRoom(int id){
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            if (lobbyRoom.getID() == id) {
                return lobbyRoom;
            }
        }
        return null;
    }
    
    public synchronized Player getPlayer(int id){
        for (Player player : nonLobbyPlayers) {
            if (player.getID() == id) {
                return player;
            }
        }
        return null;
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
    public void notifyLobbySelection(LobbyRoom newLobbyRoom, int playerID) {
        boolean ok = true;
        LobbyRoom localLR = getLobbyRoom(newLobbyRoom.getID());
        if (localLR != null && localLR.canJoin()) {
            localLR.addPlayer(getPlayer(playerID));
            notifyPlayerConnectionListeners(getPlayer(playerID), localLR);
        } else if(localLR == null){
            lobbyRooms.add(newLobbyRoom);
        } else {
            ok = false;
        }
        networkHandler.sendJoinRoomAckMessage(ok, playerID);
    }

    @Override
    public void addPlayerConnectionListener(PlayerConnectionListener playerConnectionListener) {
        playerConnectionListeners.add(playerConnectionListener);
    }
    
    /**
     * not the same as connectionAdded. 
     * This is for when a player connects to a lobby room.
     * @param p 
     */
    private void notifyPlayerConnectionListeners(Player p, LobbyRoom lobbyRoom){
        for (PlayerConnectionListener playerConnectionListener : playerConnectionListeners) {
            playerConnectionListener.notifyPlayerConnection(p, lobbyRoom);
        }
    }

    /**
     * when a player connects to the lobby server.
     * @param server
     * @param conn 
     */
    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        //Create new Player object
        nonLobbyPlayers.add(new Player(conn.getId()));
        for (LobbyRoom lobbyRoom : lobbyRooms) {
            notifyLobbyListeners(lobbyRoom);
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
