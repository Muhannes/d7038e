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
import network.service.gamesetup.AllReadyEmitter;
import network.service.gamesetup.AllReadyListener;
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
public class HostedGameSetupService extends AbstractHostedConnectionService implements AllReadyEmitter{
    
    private static final Logger LOGGER = Logger.getLogger(HostedGameSetupService.class.getName());
    
    private RmiHostedService rmiHostedService;
    private int channel;
    private GameSetupSessionImpl session;
    private boolean initialized = false;    
    private Node playersNode;
//    private BulletAppState bulletAppState;
//    private AssetManager assetManager;
    
    private final List<AllReadyListener> readyListeners = new ArrayList<>();
    private final Map<Integer, String> expectedPlayers = new HashMap<>();
    private List<Player> players = new ArrayList<>();
    private final List<Integer> readyPlayers = new ArrayList<>();
    private final List<GameSetupSessionImpl> sessions = new ArrayList<>();    
    private final List<GameSetupSessionListener> listeners = new ArrayList<>();
    private final List<Vector3f> positions = new ArrayList<>();

    public HostedGameSetupService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedGameSetupService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("HostedSetupService requires an RMI service.");
        }
        
//        createWorld(assetManager, bulletAppState);
        
    }
    
    public void setInitialized(List<Player> playerInitInfo){
        players = playerInitInfo;
        initialized = true;
    }

/*    
    public void setAssetManager(AssetManager assetManager){
        this.assetManager = assetManager;
    }
    
    public void setBulletAppState(BulletAppState bulletAppState){
        this.bulletAppState = bulletAppState;
    }
*/    
    public void addGameSetupSessionListener(GameSetupSessionListener listener){
        listeners.add(listener);
    }
    
    public void removeGameSetupSessionListener(GameSetupSessionListener listener){
        listeners.remove(listener);
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
    
/*    public void createWorld(AssetManager assetManager, BulletAppState bulletAppState){
        Spatial creepyhouse = assetManager.loadModel("Scenes/creepyhouse.j3o");
        
        if (bulletAppState != null) {
            WorldCreator.addPhysicsToMap(bulletAppState, creepyhouse);
        } else {
            LOGGER.severe("bulletAppState Was null when initializing world");
        }

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");        
        playersNode = WorldCreator.createPlayers(players, bulletAppState, mat);
    }
*/    
    private GameSetupSessionListener getCallback(HostedConnection connection){
        LOGGER.log(Level.INFO, "hostedConnection {0}\n rmi {1}", new Object[]{connection, rmiHostedService.getRmiRegistry(connection)});
        return NetConfig.getCallback(rmiHostedService.
                        getRmiRegistry(connection), GameSetupSessionListener.class);
    }
    
    private void postAllReady(){
        readyListeners.forEach(l -> l.notifyAllReady());
        sessions.forEach(s -> getCallback(s.connection).startGame());    
    }

    @Override
    public void addListener(AllReadyListener listener) {
        readyListeners.add(listener);
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
                    LOGGER.log(Level.INFO, "{0} was authenticated.", globalID);
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
                LOGGER.log(Level.WARNING, "Join failed, was not initialized, authenticated or has already joined!\ninit: {0}", initialized);
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
