/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import api.LobbyListener;
import api.LobbySelectionListener;
import api.PlayerConnectionEmitter;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.List;
import server.lobby.network.LobbyNetworkStates;
import server.lobby.network.NetworkHandler;

/**
 *
 * @author truls
 */
public class LobbyApplication implements LobbySelectionListener, PlayerConnectionEmitter, ConnectionListener{
    List<PlayerConnectionListener> playerConnectionListeners = new ArrayList<>();
    List<LobbyListener> lobbyListeners = new ArrayList<>();
    List<Player> nonLobbyPlayers = new ArrayList<>();
    
    NetworkHandler networkHandler;
    LobbyHolder lobbyHolder;

    public LobbyApplication(NetworkHandler networkHandler, LobbyHolder lobbyHolder) {
        this.networkHandler = networkHandler;
        this.lobbyHolder = lobbyHolder;
    }
    
    private Player getNonLobbyPlayer(int id){
        for (Player player : nonLobbyPlayers) {
            if (player.getID() == id) {
                return player;
            }
        }
        return null;
    }
    
    private void removePlayer(int id){
        nonLobbyPlayers.remove(getNonLobbyPlayer(id));
    }

    /**
     * Adds the player to the lobby room if it exists and is not full.
     * If the rooms does not exist, use the given lobbyRoom.
     * If both above options wont work, send back joinack false.
     * @param newLobbyRoom
     * @param playerID 
     */
    @Override
    public void notifyLobbySelection(LobbyRoom newLobbyRoom, int playerID) {
        boolean ok = true;
        Player player = getNonLobbyPlayer(playerID);
        int returnID = newLobbyRoom.getID();
        LobbyRoom localLR = lobbyHolder.getLobbyRoom(newLobbyRoom.getID());
        if (localLR != null) {
            if (localLR.removePlayer(playerID)) {
                returnID = -1;
            } else {
                localLR.addPlayer(player); // Add to room
                removePlayer(playerID); // remove from nonlobby list
            }
            notifyPlayerConnectionListeners(player, localLR);
        } else if(localLR == null){
            lobbyHolder.addLobbyRoom(newLobbyRoom); // add new lobbyRoom to list
        } else {
            ok = false;
        }
        networkHandler.sendJoinRoomAckMessage(ok, playerID, returnID);
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
        nonLobbyPlayers.add(new Player(conn.getId(), "John Doe")); //TODO: Change name 
        conn.setAttribute(LobbyNetworkStates.ROOM_ID, -1);
        // TODO: Notify the new player about available rooms!
        /*for (LobbyRoom lobbyRoom : lobbyHolder.getRooms()) {
            notifyLobbyListeners(lobbyRoom);
        }*/
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
