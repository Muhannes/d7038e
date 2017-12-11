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
import java.util.HashMap;
import java.util.Map;
import network.services.gamesetup.SetupGameEvent;
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
    
    private int everyoneIsReady = 0;
    
    public HostedLobbyService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedLobbyService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
        lobbyHolder = new LobbyHolder();
        EventBus.subscribe(this);
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
        public List<String> join(int roomid) {
            System.out.println("Player joining (HostedLobbyService)!");
            if (lobbyRoom == null) {
                LobbyRoom lr = lobbyHolder.getLobbyRoom(roomid);
                if (lr != null) {
                    boolean joined = lr.addPlayer(connection);
                    if(joined){
                        nonLobbyPlayers.remove(connection);
                        lobbyRoom = lr;
                        List<HostedConnection> players = lobbyRoom.getPlayers();
                        String name = ""+connection.getAttribute(ConnectionAttribute.NAME);
                        System.out.println("New player name: " + name);
                        for (HostedConnection player : players) {
                            // Send out to each player in room that this one has joined it.
                            if (player != connection) {
                                getDelegate(player).playerJoinedLobby(name);
                            }
                        }
                        for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                            getDelegate(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
                                    lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                        }
                        return lobbyRoom.getPlayerNames();
                    }
                }
                
            }
            return null;
        }

        @Override
        public void leave() {
            System.out.println("Player leaving (HostedLobbyService)!");
            if (lobbyRoom != null) {
                lobbyRoom.removePlayer(connection);
                List<HostedConnection> players = lobbyRoom.getPlayers();
                for (HostedConnection player : players) {
                    // Send out to each player in room that this one has joined it.
                    getDelegate(player).playerLeftLobby(connection.getAttribute(ConnectionAttribute.NAME));
                }
                for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                    getDelegate(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
                            lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                }
                if(lobbyRoom.getNumPlayers() == 0){
                    removeLobby(lobbyRoom.getName());
                }
                lobbyRoom = null;
                nonLobbyPlayers.add(connection);
                
            }
        }
        
        @Override
        public void ready(){
            System.out.println("Player is ready (HostedLobbyService)!");
            boolean allReady = lobbyRoom.setPlayerReady(connection.getId());
            
            List<HostedConnection> players = lobbyRoom.getPlayers();
            
            for (HostedConnection player : players) {
                // Send out to each player in room that this one is ready.
                System.out.println("sending to one player");
                getDelegate(players.get(0)).playerReady(connection.getAttribute(ConnectionAttribute.NAME), true);
            }
            System.out.println("How many are ready ? " + everyoneIsReady + " / " + lobbyRoom.getPlayers().size());
            if(allReady){
                System.out.println("Players are ready on server-side");
                // TODO: Start game.
                Map<Integer, String> playerInfo = new HashMap<>();
                List<Integer> ids = lobbyRoom.getPlayerIDs();
                List<String> names = lobbyRoom.getPlayerNames();
                for (int i = 0; i < names.size(); i++) {
                    String name = names.get(i);
                    int id =ids.get(i);
                    playerInfo.put(id, name);
                }
//                EventBus.publish(new SetupGameEvent(playerInfo), SetupGameEvent.class);
                for (HostedConnection player : players) {
                    System.out.println("How many times do the cow say moo ?");
                    // Send out to each player in room that this one is ready.
                    getDelegate(player).allReady();
                }
            }
        }

        @Override
        public boolean createLobby(String lobbyName) {
            LobbyRoom lr = new LobbyRoom(lobbyName);
            boolean ok = lobbyHolder.addLobbyRoom(lr);
            if(ok){
                lobbyRoom = lr;
                nonLobbyPlayers.remove(connection);
                lobbyRoom.addPlayer(connection);
                for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                    getDelegate(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
                            lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                }
                System.out.println("Done updating listeners!");
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public boolean removeLobby(String lobbyName){
            return lobbyHolder.removeLobbyRoom(lobbyName);
        }

        @Override
        public Map<String, Integer> getAllRooms() {
            Map<String, Integer> rooms = new HashMap<>();
            for (LobbyRoom room : lobbyHolder.getRooms()) {
                rooms.put(room.getName(), room.getID());
            }
            return rooms;
        }
        
    }
 
}
