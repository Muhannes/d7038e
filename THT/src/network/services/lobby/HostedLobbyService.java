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
import com.sun.istack.internal.logging.Logger;
import network.services.gamesetup.SetupGameEvent;
import network.services.login.Account;
import network.services.login.LoginListenerService;
import network.util.ConnectionAttribute;
import network.util.NetConfig;
import utils.eventbus.EventBus;

/**
 *
 * @author truls
 */
public class HostedLobbyService extends AbstractHostedConnectionService{
    
    private static final Logger LOGGER = Logger.getLogger(HostedLobbyService.class);
    
    private static final String LOBBY_SERVICE = "lobby_service";
    
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
        lobbyHolder = new LobbyHolder();
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("LobbyService requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        nonLobbyPlayers.add(connection);
        NetConfig.networkDelay(30);
        LobbyManagerImpl lobbyManager = new LobbyManagerImpl(connection);
        connection.setAttribute(LOBBY_SERVICE, lobbyManager);
        
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        rmi.share((byte)channel, lobbyManager, LobbyManager.class);
    }
    
    private ClientLobbyListener getCallback(HostedConnection connection){
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        return NetConfig.getCallback(rmi, ClientLobbyListener.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LobbyManagerImpl lobbyManagerImpl = connection.getAttribute(LOBBY_SERVICE);
        lobbyManagerImpl.leave();
        nonLobbyPlayers.remove(connection);
        
    }
    
    private class LobbyManagerImpl implements LobbyManager{
        
        private HostedConnection connection;
        private LobbyRoom lobbyRoom;
        private boolean authenticated = false;
        private Account thisAccount;
        
        public LobbyManagerImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public List<String> join(int roomid) {
            if (!authenticated) {
                return null;
            }
            LOGGER.info("Player joining (HostedLobbyService)!");
            if (lobbyRoom == null) {
                LobbyRoom lr = lobbyHolder.getLobbyRoom(roomid);
                if (lr != null) {
                    boolean joined = lr.addPlayer(connection);
                    if(joined){
                        nonLobbyPlayers.remove(connection);
                        lobbyRoom = lr;
                        List<HostedConnection> players = lobbyRoom.getPlayers();
                        for (HostedConnection player : players) {
                            // Send out to each player in room that this one has joined it.
                            if (player != connection) {
                                getCallback(player).playerJoinedLobby(thisAccount.name);
                            }
                        }
                        for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                            getCallback(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
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
            if (!authenticated) {
                return;
            }
            LOGGER.info("Player leaving (HostedLobbyService)!");
            if (lobbyRoom != null) {
                lobbyRoom.removePlayer(connection);
                List<HostedConnection> players = lobbyRoom.getPlayers();
                for (HostedConnection player : players) {
                    // Send out to each player in room that this one has joined it.
                    getCallback(player).playerLeftLobby(thisAccount.name);
                }
                for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                    getCallback(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
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
            if (!authenticated){
                return;
            }
            LOGGER.info("Player is ready (HostedLobbyService)!");
            boolean allReady = lobbyRoom.setPlayerReady(connection.getId());
            
            List<HostedConnection> players = lobbyRoom.getPlayers();
            
            for (HostedConnection player : players) {
                // Send out to each player in room that this one is ready.
                getCallback(player).playerReady(thisAccount.name, true);
            }
            if(allReady){
                LOGGER.info("All players are ready");
                // TODO: Start game.
                Map<Integer, String> playerInfo = new HashMap<>();
                List<Integer> ids = lobbyRoom.getPlayerIDs();
                ids.forEach(id -> System.out.println(id));
                List<String> names = lobbyRoom.getPlayerNames();
                for (int i = 0; i < names.size(); i++) {
                    String name = names.get(i);
                    int id =ids.get(i);
                    playerInfo.put(id, name);
                }
                List<ClientLobbyListener> callbacks = new ArrayList<>();
                LOGGER.info("players in room: " + players.size());
                for (HostedConnection player : players) {
                    // Send out to each player in room that all are ready.
                    callbacks.add(getCallback(player));
                    LOGGER.info("Adding player callback: " + player.getId());
                }
                EventBus.publish(new SetupGameEvent(playerInfo, callbacks), SetupGameEvent.class);
                
            }
        }

        @Override
        public int createLobby(String lobbyName) {
            if (!authenticated){
                return -1;
            }
            LobbyRoom lr = new LobbyRoom(lobbyName);
            boolean ok = lobbyHolder.addLobbyRoom(lr);
            if(ok){
                LOGGER.fine("Creating new lobbyRoom.");
                lobbyRoom = lr;
                nonLobbyPlayers.remove(connection);
                lobbyRoom.addPlayer(connection);
                for (HostedConnection nonLobbyPlayer : nonLobbyPlayers) {
                    getCallback(nonLobbyPlayer).updateLobby(lobbyRoom.getName(), lobbyRoom.getID(), 
                            lobbyRoom.getNumPlayers(), lobbyRoom.getMaxPlayers());
                }
                return lobbyRoom.getID();
            } else {
                return -1;
            }
        }
        
        @Override
        public boolean removeLobby(String lobbyName){
            return lobbyHolder.removeLobbyRoom(lobbyName);
        }

        @Override
        public Map<String, Integer> getAllRooms() {
            LOGGER.info("Trying to fetch rooms");
            if (!authenticated){
                return null;
            }
            Map<String, Integer> rooms = new HashMap<>();
            for (LobbyRoom room : lobbyHolder.getRooms()) {
                rooms.put(room.getName(), room.getID());
            }
            return rooms;
            
        }

        @Override
        public void authenticate(int id, String key) {
            for (Account account : LoginListenerService.getAccounts()) {
                if (account.isEqual(id, key)) {
                    LOGGER.info(id + " is authenticated.");
                    authenticated = true;
                    thisAccount = account;
                    connection.setAttribute(ConnectionAttribute.ACCOUNT, account);
                    return;
                }
            }
        }
        
    }
 
}
