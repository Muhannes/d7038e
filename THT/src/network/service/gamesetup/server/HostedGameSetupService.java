/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamesetup.server;

import api.models.EntityType;
import api.models.Player;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.WorldCreator;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import network.service.gamesetup.GameSetupSession;
import network.service.gamesetup.GameSetupSessionListener;
import network.service.gamesetup.PlayerInfoEvent;
import network.service.login.Account;
import network.service.login.LoginListenerService;
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
    
    private static final Logger LOGGER = Logger.getLogger(HostedGameSetupService.class.getName());
    
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
        //createdWorld(asset, bulletAppState);
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Game setup service started. Client id: {0}", connection.getId());
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        NetConfig.networkDelay(50);
        session = new GameSetupSessionImpl(connection);
        sessions.add(session);
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
        LOGGER.log(Level.INFO, "Number of players in game: {0}", players.size());
        int monsterID = RANDOM.nextInt(players.size());
        players.get(monsterID).setType(EntityType.Monster);
        
    }
    
    /*
    private Player getPlayerByID(int id){
        for (Player player : players) {
            if (player.getID() == id) {
                return player;
            }
        }
        return null;
    }
    */
    
    public void createdWorld(AssetManager asset, BulletAppState bulletAppState){
        Spatial creepyhouse = asset.loadModel("Scenes/creepyhouse.j3o");
        
        if (bulletAppState != null) {
            WorldCreator.addPhysicsToMap(bulletAppState, creepyhouse);
        } else {
            LOGGER.severe("bulletAppState Was null when initializing world");
        }

        //Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");        
        //playersNode = WorldCreator.createPlayers(players, bulletAppState, mat);
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
    
    private GameSetupSessionListener getCallback(HostedConnection connection){
        LOGGER.log(Level.SEVERE, "hostedConnection " + connection  + "\n rmi " + rmiHostedService.getRmiRegistry(connection));
        return NetConfig.getCallback(rmiHostedService.
                        getRmiRegistry(connection), GameSetupSessionListener.class);
    }
    
    private void postAllReady(){
        sessions.forEach(s -> getCallback(s.connection).startGame());
        
    }
    
    private class GameSetupSessionImpl implements GameSetupSession {

        private final HostedConnection connection;
        private int globalID = -1;
        private boolean joined = false;
        private boolean authenticated = false;
        
        private GameSetupSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void join(int globalID, String key, String name) {
            for (Account account : LoginListenerService.getAccounts()) {
                if (account.isEqual(globalID, key)) {
                    authenticated = true;
                    connection.setAttribute(ConnectionAttribute.ACCOUNT, account);
                    break;
                }
            }
            
            if (initialized && !joined && authenticated) {
                LOGGER.info("Join received by id: " + globalID);
                this.globalID = globalID;
                // TODO: Add security to make sure no one takes another ones id!
                getCallback(connection).initPlayer(players);
                joined = true;
            } else {
                LOGGER.warning("Join failed, was not initialized, authenticated or has already joined!");
            }
        }

        @Override
        public void ready() {
            if (globalID != -1 && authenticated) { // it has joined.
                if (!readyPlayers.contains(globalID)) { // Cannot be ready twice :/
                    readyPlayers.add(globalID);
                }
                if (readyPlayers.size() == players.size()) {
                    //Send start to clients.
                    postAllReady();
                }
                
            }
            
        }
        
    }
    
}
