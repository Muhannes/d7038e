/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.EntityType;
import api.models.Player;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import network.services.lobby.ClientLobbyListener;
import network.services.login.HostedLoginService;
import network.util.ConnectionAttribute;
import network.util.NetConfig;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author hannes
 */
public class HostedGameSetupService extends AbstractHostedConnectionService implements EventListener {
    
    private static final Logger LOGGER = Logger.getLogger(HostedGameSetupService.class);
    
    private RmiHostedService rmiHostedService;
    private int channel;
    private static final Random RANDOM = new Random();
    private GameSetupSessionImpl session;
    
    private final Map<Integer, String> expectedPlayers = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Integer> readyPlayers = new ArrayList<>();
    private final List<GameSetupSessionImpl> sessions = new ArrayList<>();
    
    private boolean initialized = false;
    
    private final List<Vector3f> positions = new ArrayList<>();
    
    public HostedGameSetupService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedGameSetupService(int channel){
        this.channel = channel;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        EventBus.subscribe(this);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("HostedSetupService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Game setup service started. Client id: {0}", connection.getId());
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        NetConfig.networkDelay(50);
        session = new GameSetupSessionImpl(connection);
        sessions.add(session);
        System.out.println("Session list : " + sessions.size());
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, GameSetupSession.class);
        // Create an object that the client can reach
        
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "player disconnected from setup service: Client id: {0}", connection.getId());
        // Nothing
    }
    
    /**
     * When expected players are received, run this.
     * Creates Player objects for each participant.
     */
    private void setupGame(Map<Integer, String> expectedPlayers){
        Random random = new Random();
        for (Integer id : expectedPlayers.keySet()) {
            players.add(new Player(EntityType.Human, new Vector3f(-random.nextInt(20),2,0), new Quaternion(0, 0, 0, 0), id));
        }
        LOGGER.info("AMount of players in game: " + players.size());
        int monsterID = RANDOM.nextInt(players.size());
        players.get(monsterID).setType(EntityType.Monster);
        
    }

    private Player getPlayerByID(int id){
        for (Player player : players) {
            if (player.getID() == id) {
                return player;
            }
        }
        return null;
    }
    
    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if (T == PlayerInfoEvent.class) {
            PlayerInfoEvent playerInfoEvent = (PlayerInfoEvent) event;
            
            setupGame(playerInfoEvent.playerInfo);
            
            initialized = true;
            LOGGER.fine("Game Setup Service is initialized");
        }
    }
    
    private GameSetupSessionListener getDelegate(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        GameSetupSessionListener callback = 
                rmiRegistry.getRemoteObject(GameSetupSessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for GameSetupSessionListener");
        }
        return callback;
    }
    
    private void postAllReady(){
        System.out.println("postAllReady before");
        sessions.forEach(s -> getDelegate(s.connection).startGame());
        System.out.println("postAllReady after");
        
    }
    
    private class GameSetupSessionImpl implements GameSetupSession {

        private final HostedConnection connection;
        private int globalID = -1;
        private boolean joined = false;
        
        private GameSetupSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void join(int globalID) {
            System.out.println("Calling ready() in HGSS\n globalID = " + globalID);
            if (initialized && !joined) {
                LOGGER.fine("Join received by id: " + globalID);
                this.globalID = globalID;
                connection.setAttribute(ConnectionAttribute.GLOBAL_ID, globalID);
                // TODO: Add security to make sure no one takes another ones id!
                rmiHostedService.getRmiRegistry(connection).
                        getRemoteObject(GameSetupSessionListener.class).initPlayer(players);
                joined = true;
            } else {
                LOGGER.fine("Join failed, was not initialized(or already joined)!");
            }
        }

        @Override
        public void ready() {
            System.out.println("Calling ready() in HGSS\n globalID = " + globalID);
            if (globalID != -1) { // it has joined.
                if (!readyPlayers.contains(globalID)) { // Cannot be ready twice :/
                    readyPlayers.add(connection.getAttribute(ConnectionAttribute.GLOBAL_ID));
                }
                if (readyPlayers.size() == players.size()) {
               
                    EventBus.publish(new StartGameEvent(), StartGameEvent.class);

                    //Send start to clients.
                    postAllReady();
                }
                
            }
            
        }
        
    }
    
}
