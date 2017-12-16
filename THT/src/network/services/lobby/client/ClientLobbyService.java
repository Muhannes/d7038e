/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby.client;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.util.NetConfig;
import network.services.lobby.LobbySessionListener;
import network.services.lobby.LobbySession;

/**
 *
 * @author truls
 */
public class ClientLobbyService extends AbstractClientService implements ClientLobbyEmitter, LobbySession{
    private static final Logger LOGGER = Logger.getLogger(ClientLobbyService.class.getName());
    
    private List<LobbySessionListener> listeners = new ArrayList<>();
    
    private LobbySessionListener callback;
    
    private LobbySession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private final int channel;
    // Channel we send on, is it a port though?
    
    public ClientLobbyService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        callback = new ClientLobbyHandlerImpl();
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("LobbyService requires RMI service");
        }
        
        // Share the callback with the server
        rmiService.share((byte)channel, callback, LobbySessionListener.class);
    }
    
    private LobbySession getDelegate(){
        if(delegate == null){
            delegate = NetConfig.getDelegate(rmiService, LobbySession.class);
        }
        return delegate;
    }

    @Override
    public void addClientLobbyListener(LobbySessionListener clientLobbyListener) {
        listeners.add(clientLobbyListener);
    }

    @Override
    public List<String> join(int roomid) {
        LOGGER.log(Level.FINE, "Sending join message to server. Roomid: {0}", roomid);
        return getDelegate().join(roomid);
    }

    @Override
    public void leave() {
        LOGGER.log(Level.FINE, "Sending leave message to server");
        getDelegate().leave();
    }

    @Override
    public void ready() {
        LOGGER.log(Level.FINE, "Sending ready message to server");
        getDelegate().ready();
    }

    @Override
    public int createLobby(String lobbyName) {
        return getDelegate().createLobby(lobbyName);
    }

    @Override
    public boolean removeLobby(String lobbyName){
        return getDelegate().removeLobby(lobbyName);
    }
    
    @Override
    public Map<String, Integer> getAllRooms() {
        return getDelegate().getAllRooms();
    }
    
    @Override
    public void authenticate(int id, String key) {
        getDelegate().authenticate(id, key);
    }
    
    @Override
    public void removeClientLobbyListener(LobbySessionListener clientLobbyListener) {
        listeners.remove(clientLobbyListener);
    }
    
    private class ClientLobbyHandlerImpl implements LobbySessionListener {

        @Override
        public void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers) {
            listeners.forEach(l -> l.updateLobby(lobbyName, roomID, numPlayers, maxPlayers));
        }

        @Override
        public void playerJoinedLobby(String name) {
            LOGGER.log(Level.FINE, "{0} join lobby", name);
            listeners.forEach(l -> l.playerJoinedLobby(name));
        }

        @Override
        public void playerLeftLobby(String name) {
            LOGGER.log(Level.FINE, "{0} left lobby", name);
            listeners.forEach(l -> l.playerLeftLobby(name));
        }

        @Override
        public void playerReady(String name, boolean ready) {
            LOGGER.log(Level.FINE, "Player: {0} is ready", name);
            listeners.forEach(l -> l.playerReady(name, ready));
        }

        @Override
        public void allReady(String ip, int port) {
            LOGGER.log(Level.FINE, "Everyone is ready");
            listeners.forEach(l -> l.allReady(ip, port));
        }
        
    }
    
}
