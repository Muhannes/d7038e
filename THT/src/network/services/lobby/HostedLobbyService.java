/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import server.lobby.network.*;
import api.LobbyListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.util.List;
import api.Player;
import api.models.LobbyRoom;
import api.models.PlayerImpl;
import com.jme3.network.MessageConnection;
import java.util.ArrayList;

/**
 *
 * @author truls
 */
public class HostedLobbyService extends AbstractHostedConnectionService{
    
    private LobbyHolder lobbyHolder;
    private final List<HostedConnection> nonLobbyPlayers = new ArrayList<>();
    
    private RmiHostedService rmiService;
    // Used to sync with client and send data
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public HostedLobbyService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedLobbyService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("LobbyService requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        nonLobbyPlayers.add(connection);
        LobbyManagerImpl lobbyManager = new LobbyManagerImpl(connection);
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        rmi.share((byte)channel, lobbyManager, LobbyManager.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection hc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private class LobbyManagerImpl implements LobbyManager{
        
        private HostedConnection connection;
        private LobbyRoom lobbyRoom;
        
        public LobbyManagerImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public LobbyRoom join(int roomid) {
            if (lobbyRoom == null) {
                PlayerImpl p = connection.getAttribute(LobbyNetworkStates.PLAYER);
                LobbyRoom lr = lobbyHolder.getLobbyRoom(roomid);
                boolean joined = lr.addPlayer(p);
                if(joined){
                    nonLobbyPlayers.remove(connection);
                    lobbyRoom = lr;
                    return lobbyRoom;
                }
            }
            return null;
        }

        @Override
        public void leave() {
            if (lobbyRoom != null) {
                lobbyRoom.removePlayer(connection.getId());
                lobbyRoom = null;
                nonLobbyPlayers.add(connection);
            }
        }
        
        @Override
        public void ready(){
            boolean allReady = lobbyRoom.setPlayerReady(connection.getId());
            if (allReady) {
                // TODO: Start game.
            }
        }
        
    }
 
}
