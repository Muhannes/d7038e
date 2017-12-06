/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import api.models.LobbyRoom;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author truls
 */
public class ClientLobbyService extends AbstractClientService implements ClientLobbyEmitter, LobbyManager{
    
    private List<ClientLobbyListener> listeners = new ArrayList<>();
    
    private ClientLobbyListener callback;
    
    private LobbyManager delegate;
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
        rmiService.share((byte)channel, callback, ClientLobbyListener.class);
    }
    
    private LobbyManager getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(LobbyManager.class);
            if( delegate == null ) {
                throw new RuntimeException("No LobbyManager found");
            } 
        }
        return delegate;
    }

    @Override
    public void addClientLobbyListener(ClientLobbyListener clientLobbyListener) {
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
        getDelegate().ready();
    }

    @Override
    public boolean createLobby(String lobbyName) {
        return getDelegate().createLobby(lobbyName);
    }

    @Override
    public Map<String, Integer> getAllRooms() {
        return getDelegate().getAllRooms();
    }
    
    private class ClientLobbyHandlerImpl implements ClientLobbyListener { //TODO implement some kind of listeners

        @Override
        public void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers) {
            for (ClientLobbyListener listener : listeners) {
                listener.updateLobby(lobbyName, roomID, numPlayers, maxPlayers);
            }
        }

        @Override
        public void playerJoined(String name) {
            for (ClientLobbyListener listener : listeners) {
                listener.playerJoined(name);
            }
        }

        @Override
        public void playerLeft(String name) {
            for (ClientLobbyListener listener : listeners) {
                listener.playerLeft(name);
            }
        }

        @Override
        public void playerReady(String name, boolean ready) {
            for (ClientLobbyListener listener : listeners) {
                listener.playerReady(name, ready);
            }
        }
        
        
    }
    
    
    
}
