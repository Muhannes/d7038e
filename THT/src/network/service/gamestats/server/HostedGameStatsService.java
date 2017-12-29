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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import network.gameserver.GameServer;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionEmitter;
import network.service.gamestats.GameStatsSessionListener;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class HostedGameStatsService extends AbstractHostedConnectionService implements GameStatsSessionEmitter{

    private static final Logger LOGGER = Logger.getLogger(HostedGameStatsService.class);
    
    private static final String GAME_STATS_SERVICE = "game_stats_service";
    
    private RmiHostedService rmiHostedService;
    
    private final List<GameStatsSessionImpl> players = new ArrayList<>();
    //private final ArrayList<GameStatsSessionListener> listeners = new ArrayList<>();
    private final List<GameStatsSession> gameStatsSessions = new ArrayList<>();
    private final List<String> trapNames = new ArrayList<>();
    private final List<Vector3f> trapPositions = new ArrayList<>();
    
    private List <String> updatedTraps = new ArrayList<>();
    
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
    }
    
    public void trapUpdated(String id){
        LOGGER.log(Level.INFO, "New trap received : " + id);
        if(!updatedTraps.contains(id)){
            updatedTraps.add(id);
        }
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service started. Client id: {0}", connection.getId());
        NetConfig.networkDelay(30);

        // Retrieve the client side callback
        
//        GameStatsSessionListener callback = getCallback(connection);
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
    
/*    private GameStatsSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        GameStatsSessionListener callback = rmiRegistry.getRemoteObject(GameStatsSessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for gameStatsSessionListener");
        }
        return callback;
    }
*/    
    @Override
    public void addSessions(GameStatsSession session){
        gameStatsSessions.add(session);
    }
    
    @Override
    public void removeSessions(GameStatsSession session){
        gameStatsSessions.remove(session);
    }
    
    public void sendOutTraps(Node trapNode, Node playersNode){
        //Send out movements everything 10ms 
        new Thread(
            new Runnable(){
                @Override
                public void run() {
                    while(true){
                        try {                    
                            Thread.sleep(20);                    
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {                            
                            for(String newTrapId : updatedTraps){            
                                Vector3f position = trapNode.getChild(newTrapId).getLocalTranslation();
                                String trapName = trapNode.getChild(newTrapId).getName();
                                trapNames.add(trapName);
                                trapPositions.add(position);
                                if(!trapNames.isEmpty() && !trapPositions.isEmpty()){
                                    broadcast(trapNames, trapPositions);
                                    //Clearing old traps
                                    trapNames.clear();
                                    trapPositions.clear();
                                }
                            }
                        }                    
                    }
                }            
            }
        ).start();
    }

    public void broadcast(List<String> trapNames, List<Vector3f> trapLocations){
        players.forEach(l -> l.getCallback().notifyTrapsPlaced(trapNames, trapLocations));
    }
    
    private class GameStatsSessionImpl implements GameStatsSession {
        
        private final HostedConnection connection; //Used for what?
        private GameStatsSessionListener callback; //Used for what?
        
        public GameStatsSessionImpl(HostedConnection connection){
            this.connection = connection;
//            this.callback = callback;
        }
        
        private GameStatsSessionListener getCallback(){
            if(callback == null){
                RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
                callback = NetConfig.getCallback(rmiRegistry, GameStatsSessionListener.class);
            }
            return callback;
        }
        
        @Override
        public void notifyPlayerKilled(String victim, String killer) {
            gameStatsSessions.forEach(l -> l.notifyPlayerKilled(victim, killer));
        }

        @Override
        public void notifyPlayerEscaped(String name) {
            gameStatsSessions.forEach(l -> l.notifyPlayerEscaped(name));
        }

        @Override
        public void notifyTrapPlaced(String trapName, Vector3f newTrap) {
            LOGGER.log(Level.INFO, "trap received at server");
            gameStatsSessions.forEach(l -> l.notifyTrapPlaced(trapName, newTrap));
        }

        @Override
        public void notifyTrapTriggered(String name, String trapName) {
            gameStatsSessions.forEach(l -> l.notifyTrapTriggered(name, trapName));
        }

/*        @Override
        public void notifyPlayersKilled(List<String> victims, List<String> killers) {
            listeners.forEach(l -> l.notifyPlayersKilled(victims, killers));
        }

        @Override
        public void notifyPlayersEscaped(List<String> names) {
            listeners.forEach(l -> l.notifyPlayersEscaped(names));
        }

        @Override
        public void notifyTrapsPlaced(List<String> trapNames, List<Vector3f> newTraps) {
            listeners.forEach(l -> l.notifyTrapsPlaced(trapNames, newTraps));
        }

        @Override
        public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
            listeners.forEach(l -> l.notifyTrapsTriggered(names, trapNames));
        }
*/
    } 
}
