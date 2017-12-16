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
import com.sun.istack.internal.logging.Logger;
import network.util.NetConfig;
import network.services.lobby.LobbySessionListener;
import network.services.lobby.LobbySession;

/**
 *
 * @author truls
 */
public class ClientLobbyService extends AbstractClientService implements ClientLobbyEmitter, LobbySession{
    private static final Logger LOGGER = Logger.getLogger(ClientLobbyService.class);
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
        return getDelegate().join(roomid);
    }

    @Override
    public void leave() {
        getDelegate().leave();
    }

    @Override
    public void ready() {
        LOGGER.fine("Pressed ready! (clientlobbyservice) ");
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

    
    private class ClientLobbyHandlerImpl implements LobbySessionListener { //TODO implement some kind of listeners

        @Override
        public void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers) {
            for (LobbySessionListener listener : listeners) {
                listener.updateLobby(lobbyName, roomID, numPlayers, maxPlayers);
            }
        }

        @Override
        public void playerJoinedLobby(String name) {
            LOGGER.fine(name + " join message received.");
            for (LobbySessionListener listener : listeners) {
                listener.playerJoinedLobby(name);
            }
        }

        @Override
        public void playerLeftLobby(String name) {
            for (LobbySessionListener listener : listeners) {
                listener.playerLeftLobby(name);
            }
        }

        @Override
        public void playerReady(String name, boolean ready) {
            LOGGER.fine("Player : " + name + " is ready.");
            for (LobbySessionListener listener : listeners) {
                listener.playerReady(name, ready);
            }
        }

        @Override
        public void allReady(String ip, int port) {
            LOGGER.fine("Everyone is ready!");
            for (LobbySessionListener listener : listeners) {
                listener.allReady(ip, port);
            }
        }
    }
}