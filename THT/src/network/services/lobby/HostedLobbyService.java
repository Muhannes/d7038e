/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import com.jme3.network.HostedConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.util.List;
import api.models.LobbyRoom;
import com.jme3.network.MessageConnection;
import java.util.ArrayList;
import network.services.login.LoginEvent;
import network.util.ConnectionAttribute;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author truls
 */
public class HostedLobbyService extends AbstractHostedConnectionService implements EventListener{
    
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
        setAutoHost(false);
        EventBus.subscribe(this);
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("LobbyService requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        nonLobbyPlayers.add(connection);
        System.out.println("Starting connection1");
        
        LobbyManagerImpl lobbyManager = new LobbyManagerImpl(connection);
        
        System.out.println("Starting connection2");
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        
        System.out.println("Starting connection3");
        
        rmi.share((byte)channel, lobbyManager, LobbyManager.class);
        
        System.out.println("Starting connection4");
    }
    
    private ClientLobbyListener getDelegate(HostedConnection connection){
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        ClientLobbyListener delegate = rmi.getRemoteObject(ClientLobbyListener.class);
        if( delegate == null ) {
            throw new RuntimeException("No chat session found");
        }
        return delegate;
    }

    @Override
    public void stopHostingOnConnection(HostedConnection hc) {
        //TODO: quit player
        nonLobbyPlayers.remove(hc);
    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if (T == LoginEvent.class) {
            startHostingOnConnection(((LoginEvent)event).conn);
        }
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
                LobbyRoom lr = lobbyHolder.getLobbyRoom(roomid);
                boolean joined = lr.addPlayer(connection);
                if(joined){
                    nonLobbyPlayers.remove(connection);
                    lobbyRoom = lr;
                    List<HostedConnection> players = lobbyRoom.getPlayers();
                    for (HostedConnection player : players) {
                        // Send out to each player in room that this one has joined it.
                        getDelegate(player).
                                playerJoined(connection.getAttribute(ConnectionAttribute.NAME));
                    }
                    for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                        getDelegate(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), 
                                lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                    }
                    return lobbyRoom;
                }
            }
            return null;
        }

        @Override
        public void leave() {
            if (lobbyRoom != null) {
                lobbyRoom.removePlayer(connection);
                List<HostedConnection> players = lobbyRoom.getPlayers();
                for (HostedConnection player : players) {
                    // Send out to each player in room that this one has joined it.
                    getDelegate(player).
                            playerLeft(connection.getAttribute(ConnectionAttribute.NAME));
                }
                for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                    getDelegate(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), 
                            lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                }
                lobbyRoom = null;
                nonLobbyPlayers.add(connection);
                
            }
        }
        
        @Override
        public void ready(){
            boolean allReady = lobbyRoom.setPlayerReady(connection.getId());
            List<HostedConnection> players = lobbyRoom.getPlayers();
            for (HostedConnection player : players) {
                // Send out to each player in room that this one has joined it.
                getDelegate(player).
                        playerReady(connection.getAttribute(ConnectionAttribute.NAME), true);
            }
            if (allReady) {
                // TODO: Start game.
            }
        }
        
    }
 
}
