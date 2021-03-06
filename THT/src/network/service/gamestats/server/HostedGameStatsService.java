/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats.server;

import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionEmitter;
import network.service.gamestats.GameStatsSessionListener;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class HostedGameStatsService extends AbstractHostedConnectionService implements GameStatsSessionEmitter{

    private static final Logger LOGGER = Logger.getLogger(HostedGameStatsService.class.getName());
    
    private static final String GAME_STATS_SERVICE = "game_stats_service";
    
    private RmiHostedService rmiHostedService;
    
    private final List<GameStatsSessionImpl> players = new ArrayList<>();
    private final List<GameStatsSession> gameStatsSessions = new ArrayList<>();

    //For traps
    private final List<String> trapNames = new ArrayList<>();
    private final List<Vector3f> trapPositions = new ArrayList<>();
    
    //When traps are triggered
    private final List<String> triggers = new ArrayList<>();
    private final List<String> triggeredTraps = new ArrayList<>();
    
    private final List <String> updatedTraps = new ArrayList<>();
    private final List <String> deletedTraps = new ArrayList<>();
    private final List <String> slowedPlayers = new ArrayList<>();
    
    private ExecutorService executor;
    private int channel;
    
    public HostedGameStatsService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedGameStatsService(int channel){
        this.channel = channel;
    }   

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        //setAutoHost(false);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("GameStats service requires an RMI service.");
        }
        executor = Executors.newCachedThreadPool();
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service started. Client id: {0}", connection.getId());
        NetConfig.networkDelay(30);

        // The newly connected client will be represented by this object on
        // the server side
        GameStatsSessionImpl session = new GameStatsSessionImpl(connection);
        players.add(session);
        
        connection.setAttribute(GAME_STATS_SERVICE, session);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, GameStatsSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service stopped: Client id: {0}", connection.getId());
    }
    
    @Override
    public void addSessions(GameStatsSession session){
        gameStatsSessions.add(session);
    }
    
    @Override
    public void removeSessions(GameStatsSession session){
        gameStatsSessions.remove(session);
    }
    
    public void sendOutTraps(Node trapNode, String id){
        //Send out movements everything 10ms 
        Runnable r = new Runnable(){
            @Override
            public void run() {                         
                String trapName = trapNode.getChild(id).getName();
                Vector3f position = trapNode.getChild(id).getLocalTranslation();
                broadcast(trapName, position);
            }            
        };
        executor.submit(r);
        
    }

    public void broadcast(String trapName, Vector3f trapLocation){
        players.forEach(l -> l.getCallback().notifyTrapsPlaced(trapName, trapLocation));
    }
    
    public void sendOutDeletedTraps(String triggerer, String triggeredTrap){
        //Send out movements everything 10ms 
        
        Runnable r = new Runnable(){
            @Override
            public void run() {
                broadcastDeletedTraps(triggerer, triggeredTrap);
            }
        };
        executor.submit(r);
    }

    public void broadcastDeletedTraps(String triggerer, String triggeredTrap){
        players.forEach(l -> l.getCallback().notifyTrapsTriggered(triggerer, triggeredTrap));            
    }
    
    
    public void sendOutKilled(String victim, String killer){
        Runnable r = new Runnable(){
            @Override
            public void run(){
                broadcastPlayersKilled(victim, killer);
            }
        };
        executor.submit(r);
    }
    
    public void broadcastPlayersKilled(String victim, String killer){
        players.forEach(l -> l.getCallback().notifyPlayersKilled(victim, killer));
    }
    
    public void sendOutMonkeyInfo(String catcher, String monkey){
        Runnable r = new Runnable(){
            @Override
            public void run() {
                broadcastMonkeysCaught(catcher, monkey);
            }
        };
        executor.submit(r);
    }

    public void broadcastMonkeysCaught(String catcher, String monkey){
        players.forEach(l -> l.getCallback().notifyMonkeysCaught(catcher, monkey));        
    }
    
    public void gameover(String winners){
        Runnable r = new Runnable(){
            @Override
            public void run() {
                players.forEach(l -> l.getCallback().notifyGameOver(winners));
            }
        };
        executor.submit(r);
    }
    
    public void broadcastSlash(String player){
        Runnable r = new Runnable(){
            @Override
            public void run() {
                players.forEach(l -> l.getCallback().notifyPlayerSlashed(player));
            }
        };
        executor.submit(r);        
    }
    
    public void broadcastJump(String player){
        Runnable r = new Runnable(){
            @Override
            public void run() {
                players.forEach(l -> l.getCallback().notifyPlayerJumped(player));
            }
        };
        executor.submit(r);
    }
    
    private class GameStatsSessionImpl implements GameStatsSession {
        
        private final HostedConnection connection;
        private GameStatsSessionListener callback; 
        
        public GameStatsSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        public GameStatsSessionListener getCallback(){
            if(callback == null){
                RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
                callback = NetConfig.getCallback(rmiRegistry, GameStatsSessionListener.class);
            }
            return callback;
        }
        
        @Override
        public void notifyTrapPlaced(String trapName, Vector3f newTrap) {
            gameStatsSessions.forEach(l -> l.notifyTrapPlaced(trapName, newTrap));
        }

        @Override
        public void notifyJump(String player) {            
            gameStatsSessions.forEach(l -> l.notifyJump(player));
        }

        @Override
        public void notifySlash(String player) {
            gameStatsSessions.forEach(l -> l.notifySlash(player));
        }
    } 
}
