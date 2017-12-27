/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.lobby.LobbyRoom;
import network.service.lobby.LobbySession;
import network.service.lobby.LobbySessionListener;
import network.service.login.Account;
import network.service.login.LoginListenerService;
import network.util.ConnectionAttribute;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class LobbySessionImpl implements LobbySession, LobbySessionListener{
    
    private static final Logger LOGGER = Logger.getLogger(LobbySessionImpl.class.getName());
    
    private HostedConnection connection;
    private RmiHostedService rmi;
    private LobbySessionListener callback;
    
    private boolean authenticated;
    private Account account;
    private String joinedRoom;
    
    LobbySessionImpl(HostedConnection connection, RmiHostedService rmi){
        this.connection = connection;
        this.rmi = rmi;
    }
    
    Account getAccount(){
        return account;
    }
    
    String getName(){
        return account.name;
    }
    
    int getId(){
        return account.id;
    }

    @Override
    public void authenticate(int id, String key) {
        for (Account account : LoginListenerService.getAccounts()) {
            if (account.isEqual(id, key)) {
                LOGGER.info(id + " is authenticated.");
                authenticated = true;
                this.account = account;
                connection.setAttribute(ConnectionAttribute.ACCOUNT, account);
                return;
            }
        }
    }

    @Override
    public void join(String room) {
        if (!authenticated) {
            LOGGER.log(Level.INFO, "Could not join lobby room {0} because player is not authenticated.", room);
            return;
        }
        
        if(room.equals("")) return;
        
        LobbyRoomImpl r = (LobbyRoomImpl)LobbyRoomImpl.getRoom(room);
        joinedRoom = r.join(this);
        
        if(joinedRoom != null) {
            LOGGER.log(Level.INFO, "Player {0} joined lobby room {1}", 
                    new Object[]{account.name, joinedRoom});
        }
    }

    @Override
    public void leave() {
        if (!authenticated) {
            LOGGER.log(Level.INFO, "Could not leave lobby room because player is not authenticated.");
            return;
        }
        
        if(joinedRoom == null) {
            LOGGER.log(Level.INFO, "Player {0} can not leave lobby room because he hasnt joined any room",
                    new Object[]{account.name});
        }
        
        LobbyRoomImpl room = (LobbyRoomImpl)LobbyRoomImpl.getRoom(joinedRoom);
        room.leave(this);
        LOGGER.log(Level.INFO, "Player {0} left lobby room {1}", new Object[]{account.name, joinedRoom});
        joinedRoom = null;
    }

    @Override
    public void ready() {
        if (!authenticated){
            LOGGER.log(Level.INFO, "Not authenticated.");
            return;
        }
        
        if(joinedRoom == null) {
            LOGGER.log(Level.INFO, "Player {0} can not be ready because he hasnt joined a lobby room",
                    new Object[]{account.name});
        }
        
        LobbyRoomImpl room = (LobbyRoomImpl)LobbyRoomImpl.getRoom(joinedRoom);
        room.ready(this);
    }
    
    @Override
    public void fetchAllRooms() {
        if (!authenticated){
            LOGGER.log(Level.INFO, "Not authenticated.");
            return;
        }
        getCallback().updateLobby(LobbyRoomImpl.getAllRooms());
    }

    @Override
    public void updateLobby(List<LobbyRoom> rooms) {
        getCallback().updateLobby(rooms);
    }

    @Override
    public void playerJoinedLobby(String name) {
        getCallback().playerJoinedLobby(name);
    }

    @Override
    public void playerLeftLobby(String name) {
        getCallback().playerLeftLobby(name);
    }

    @Override
    public void playerReady(String name, boolean ready) {
        getCallback().playerReady(name, ready);
    }

    @Override
    public void allReady(String ip, int port) {
        getCallback().allReady(ip, port);
    }

    @Override
    public void joinedLobby(LobbyRoom room) {
        getCallback().joinedLobby(room);
    }
    
    private LobbySessionListener getCallback(){
        if (callback == null){
            RmiRegistry rmiRegistry = rmi.getRmiRegistry(connection);
            callback =  NetConfig.getCallback(rmiRegistry, LobbySessionListener.class);
        }
        return callback;
    }
    
}
