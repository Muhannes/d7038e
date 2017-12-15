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
    
    private final Map<Integer, String> expectedPlayers = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Integer> readyPlayers = new ArrayList<>();
    private boolean initialized = false;
    
    public HostedGameSetupService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedGameSetupService(int channel){
        this.channel = channel;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        //setAutoHost(false);
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
        // Create an object that the client can reach
        GameSetupSession session = new GameSetupSessionImpl(connection);
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, GameSetupSession.class);
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
        for (Integer id : expectedPlayers.keySet()) {
            players.add(new Player(EntityType.Human, new Vector3f(0,0,0), new Quaternion(0, 0, 0, 0), id));
        }
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
        if (T == SetupGameEvent.class) {
            SetupGameEvent setupGameEvent = (SetupGameEvent) event;
            
            setupGame(setupGameEvent.getPlayers());
            
            initialized = true;
            LOGGER.fine("Game Setup Service is initialized");
        }
    }
    
    /**
     * TODO: Add authentication
     */
    private class GameSetupSessionImpl implements GameSetupSession {

        private final HostedConnection connection;
        private int globalID = -1;
        private boolean joined = false;
        
        private GameSetupSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void join(int globalID, String key, String name) {
            if (initialized && !joined) {
                LOGGER.fine("Join received by id: " + globalID);
                this.globalID = globalID;
                // TODO: Add security to make sure no one takes another ones id!
                Player p = getPlayerByID(globalID);
                NetConfig.getCallback(rmiHostedService.
                        getRmiRegistry(connection), GameSetupSessionListener.class).initPlayer(players);
                joined = true;
            } else {
                LOGGER.warning("Join failed, was not initialized(or already joined)!");
            }
        }

        @Override
        public void ready() {
            if (globalID != -1) { // it has joined.
                if (!readyPlayers.contains(globalID)) { // Cannot be ready twice :/
                    readyPlayers.add(globalID);
                }
                if (readyPlayers.size() == players.size()) {
                    EventBus.publish(new StartGameEvent(), StartGameEvent.class);
                    // TODO: Send start to clients.
                }
            }
            
        }
        
    }
    
}
