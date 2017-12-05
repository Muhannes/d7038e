/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import network.services.lobby.LobbyHolder;
import api.LobbyListener;
import api.LobbySelectionListener;
import api.LoginListener;
import api.PlayerConnectionEmitter;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import api.models.PlayerImpl;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.List;
import server.lobby.network.LobbyNetworkStates;
import network.services.lobby.NetworkHandler;

/**
 *
 * @author truls
 */
public class LobbyConnectionHandler implements LobbySelectionListener, PlayerConnectionEmitter, 
        ConnectionListener, LoginListener{
    List<PlayerConnectionListener> playerConnectionListeners = new ArrayList<>();
    List<LobbyListener> lobbyListeners = new ArrayList<>();
    List<PlayerImpl> nonLobbyPlayers = new ArrayList<>();
    
    NetworkHandler networkHandler;
    LobbyHolder lobbyHolder;

    public LobbyConnectionHandler(NetworkHandler networkHandler, LobbyHolder lobbyHolder) {
        this.networkHandler = networkHandler;
        this.lobbyHolder = lobbyHolder;
    }
    
    private PlayerImpl getNonLobbyPlayer(int id){
        for (PlayerImpl player : nonLobbyPlayers) {
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
     * TODO: Check so that playerconnectionListener is not needed, if not, delete it
     * @param newLobbyRoom
     * @param playerID 
     */
    @Override
    public void notifyLobbySelection(LobbyRoom newLobbyRoom, int playerID) {
        boolean ok = true;
        PlayerImpl player = getNonLobbyPlayer(playerID);
        int returnID = newLobbyRoom.getID();
        LobbyRoom localLR = lobbyHolder.getLobbyRoom(returnID);
        if (player == null){ // player wants to leave lobbyroom
            //player = lobbyHolder.removePlayer(playerID, newLobbyRoom.getID());
            if (player != null){ // Player was in room he claimed to be.
                returnID = -1;
                nonLobbyPlayers.add(player);
                // TODO: Notify player about available lobbyRooms to join.
                //notifyPlayerConnectionListeners(player, localLR);
            } else { // Player tried to leave room he was not in.
                ok = false;
            }
        } else { // Player wants to join a lobby
            if (localLR != null) { // Room exists
                //boolean joined = lobbyHolder.addPlayer(player, returnID); // add to room
                //if (joined) { // join was ok
                //    removePlayer(playerID); // remove from nonlobby list
                    //notifyPlayerConnectionListeners(player, localLR);
                //} else {
                //    ok = false;
                //}
            } else { // Player wants to create new room
                // TODO: Check best way to create new room (create new serverside, or use the one client sent)
                newLobbyRoom.clearRoom();
                newLobbyRoom.addPlayer(player);
                lobbyHolder.addLobbyRoom(newLobbyRoom);
            }   
        }
        //networkHandler.sendJoinRoomAckMessage(ok, playerID, returnID);
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
    private void notifyPlayerConnectionListeners(PlayerImpl p, LobbyRoom lobbyRoom){
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
        nonLobbyPlayers.add(new PlayerImpl(conn.getId(), "Player"+conn.getId()));
        conn.setAttribute(LobbyNetworkStates.ROOM_ID, -1);
        // Notify the new player about available rooms!
        List<HostedConnection> conns = new ArrayList<>();
        conns.add(conn);
        //networkHandler.sendLobbyRoomsMessage(lobbyHolder.getRooms(), conns);
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        // Remove it from its lobbyRoom
        //lobbyHolder.removePlayer(conn.getId(), conn.getAttribute(LobbyNetworkStates.ROOM_ID));
    }

    /**
     * Could be so much more security in this function, but this is how it works now...
     * @param playerID
     * @param username 
     */
    @Override
    public void notifyLogin(int playerID, String username) {
        PlayerImpl player = getNonLobbyPlayer(playerID);
        if (player != null) { // Player is in a room and must already have logged in...
                              //...Should maybee be a boolean to check this
            player.setName(username);
            //networkHandler.sendLoginAckMessage(true, playerID);
            
        } else {
            //networkHandler.sendLoginAckMessage(false, playerID);
        }
    }
    
}
