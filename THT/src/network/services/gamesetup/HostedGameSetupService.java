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
import network.services.login.HostedLoginService;
import network.util.ConnectionAttribute;
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
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(HostedLoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Create an object that the client can reach
        GameSetupSessionImpl session = new GameSetupSessionImpl(connection, getDelegate(connection));
        sessions.add(session);
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
        Random random = new Random();
        for (Integer id : expectedPlayers.keySet()) {
            players.add(new Player(EntityType.Human, new Vector3f(-random.nextInt(20),2,0), new Quaternion(0, 0, 0, 0), id));
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
    
    private GameSetupSessionListener getDelegate(HostedConnection connection){
         return rmiHostedService.getRmiRegistry(connection).
                        getRemoteObject(GameSetupSessionListener.class);    
    }
    
    private void postAllReady(){
        sessions.forEach(s -> s.delegate.startGame());
    }
    
    private class GameSetupSessionImpl implements GameSetupSession {

        private final HostedConnection connection;
        private final GameSetupSessionListener delegate;
        private int globalID = -1;
        private boolean joined = false;
        
        private GameSetupSessionImpl(HostedConnection connection, GameSetupSessionListener delegate){
            this.connection = connection;
            this.delegate = delegate;
        }
        
        @Override
        public void join(int globalID) {
            System.out.println("Calling ready() in HGSS\n globalID = " + globalID);
            if (initialized && !joined) {
                LOGGER.fine("Join received by id: " + globalID);
                this.globalID = globalID;
                connection.setAttribute(ConnectionAttribute.GLOBAL_ID, globalID);
                // TODO: Add security to make sure no one takes another ones id!
                Player p = getPlayerByID(globalID);
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
                    System.out.println("SENDING OUT NEW START GAME EVENT");
                    postAllReady();
                }
                
            }
            
        }
        
    }
    
}
