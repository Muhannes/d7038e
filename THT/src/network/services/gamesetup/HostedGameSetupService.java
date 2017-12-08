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
import network.services.chat.ChatSession;
import network.services.chat.HostedChatService;
import network.util.ConnectionAttribute;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author hannes
 */
public class HostedGameSetupService extends AbstractHostedConnectionService implements EventListener {
    
    private static final Logger LOGGER = Logger.getLogger(HostedChatService.class);
    
    private RmiHostedService rmiHostedService;
    private int channel;
    private static final Random RANDOM = new Random();
    
    private final Map<Integer, String> expectedPlayers = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Integer> readyPlayers = new ArrayList<>();
    private boolean initialized = false;
    
    public HostedGameSetupService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("ChatHostedService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service started. Client id: {0}", connection.getId());
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
        }
    }
    
    private class GameSetupSessionImpl implements GameSetupSession {

        private final HostedConnection connection;
        private int globalID = -1;
        
        private GameSetupSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void join(int globalID) {
            if (initialized) {
                this.globalID = globalID;
                connection.setAttribute(ConnectionAttribute.GLOBAL_ID, globalID);
                // TODO: Add security to make sure no one takes another ones id!
                Player p = getPlayerByID(globalID);
                rmiHostedService.getRmiRegistry(connection).getRemoteObject(GameSetupSessionListener.class).initPlayer(p);
            
            }
            
        }

        @Override
        public void ready() {
            if (globalID != -1) { // it has joined.
                if (!readyPlayers.contains(globalID)) { // Cannot be ready twice :/
                    readyPlayers.add(connection.getAttribute(ConnectionAttribute.GLOBAL_ID));
                }
                if (readyPlayers.size() == players.size()) {
                    EventBus.publish(new StartGameEvent(), StartGameEvent.class);
                }
            }
            
        }
        
    }
    
}
