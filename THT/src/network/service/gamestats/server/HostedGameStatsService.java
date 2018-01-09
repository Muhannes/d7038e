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
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    
    public void trapUpdated(String id){
        if(!updatedTraps.contains(id)){
            updatedTraps.add(id);
        }
    }
    
    public void triggeredTrap(String playerId, String id){        
        if(!deletedTraps.contains(id)){
            deletedTraps.add(id);
        }
        if(!slowedPlayers.contains(playerId)){
            slowedPlayers.add(playerId);
        }
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
    
    public void sendOutTraps(Node trapNode){
        //Send out movements everything 10ms 
        Runnable r = new Runnable(){
            @Override
            public void run() {                         
                for(String newTrapId : updatedTraps){            
                    Vector3f position = trapNode.getChild(newTrapId).getLocalTranslation();
                    String trapName = trapNode.getChild(newTrapId).getName();

                    trapNames.add(trapName);
                    trapPositions.add(position);
                }    

                if(!trapNames.isEmpty() && !trapPositions.isEmpty()){
                    broadcast(trapNames, trapPositions);

                    //Clearing old lists
                    trapNames.clear();
                    trapPositions.clear();
                    updatedTraps.clear(); 
                }                            

            }            
        };
        executor.submit(r);
        
    }

    public void broadcast(List<String> trapNames, List<Vector3f> trapLocations){
        players.forEach(l -> l.getCallback().notifyTrapsPlaced(trapNames, trapLocations));
    }
    
    public void sendOutDeletedTraps(){
        //Send out movements everything 10ms 
        
        Runnable r = new Runnable(){
            @Override
            public void run() {
                for(String id : deletedTraps){            
                    if(!triggeredTraps.contains(id)){
                        triggeredTraps.add(id);                                   
                    }
                }    
                for(String playerId : slowedPlayers){
                    if(!triggers.contains(playerId)){
                        triggers.add(playerId);                                    
                    }
                }
                if(triggers.size() > 0 && triggeredTraps.size() > 0){
                    LOGGER.log(Level.INFO, "Broadcasting out \n" + triggers + " \n " + triggeredTraps);
                    broadcastDeletedTraps(triggers, triggeredTraps);

                    //Clearing old lists
                    triggers.clear();
                    triggeredTraps.clear();
                    slowedPlayers.clear();
                    deletedTraps.clear();                                
                    if(deletedTraps.size() > 0 || slowedPlayers.size() > 0){
                        LOGGER.log(Level.SEVERE, "Clear not functional");
                    }
                }
            }
        };
        executor.submit(r);
    }

    public void broadcastDeletedTraps(List<String> triggers, List<String> triggeredTraps){
        players.forEach(l -> l.getCallback().notifyTrapsTriggered(triggers, triggeredTraps));            
    }
    
    
    public void sendOutKilled(String victim, String killer){
        LOGGER.log(Level.INFO, "sendOutKilled()");
        Runnable r = new Runnable(){
            @Override
            public void run(){
                LOGGER.log(Level.INFO, "boardcasting out dead player\n" + victim + "\n" + killer);
                broadcastPlayersKilled(victim, killer);   

            }
        };
        executor.submit(r);
    }
    
    public void broadcastPlayersKilled(String victim, String killer){
        LOGGER.log(Level.INFO, victim + "\n" + killer);
        players.forEach(l -> l.getCallback().notifyPlayersKilled(victim, killer));
    }
    
    public void sendOutMonkeyInfo(String catcher, String monkey){
        //broadcast out that a monkey got caught
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
        LOGGER.log(Level.SEVERE, "Sending out gameover from server!");
        Runnable r = new Runnable(){
            @Override
            public void run() {
                players.forEach(l -> l.getCallback().notifyGameOver(winners));
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
            LOGGER.log(Level.INFO, "trap received at server" + trapName + " - " + newTrap );
            gameStatsSessions.forEach(l -> l.notifyTrapPlaced(trapName, newTrap));
        }
    } 
}
