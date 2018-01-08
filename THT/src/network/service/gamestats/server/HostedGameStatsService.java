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
    
    //When player dies
    private final List <String> victims = new ArrayList<>();
    private final List <String> killers = new ArrayList<>();
    
    //when monkey is caught
    private final List<String> monkeys = new ArrayList<>();
    private final List<String> catchers = new ArrayList<>();
    
    
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
        new Thread(new Runnable(){
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
            }
        ).start();
    }

    public void broadcast(List<String> trapNames, List<Vector3f> trapLocations){
        players.forEach(l -> l.getCallback().notifyTrapsPlaced(trapNames, trapLocations));
    }
    
    public void sendOutDeletedTraps(){
        //Send out movements everything 10ms 
        new Thread(
            new Runnable(){
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
            }
        ).start();
    }

    public void broadcastDeletedTraps(List<String> triggers, List<String> triggeredTraps){
        players.forEach(l -> l.getCallback().notifyTrapsTriggered(triggers, triggeredTraps));            
    }
    
    public void playerGotKilled(String victim, String killer){
        LOGGER.log(Level.INFO, killer + " slaughtered " + victim);
        victims.add(victim);
        killers.add(killer);        
    }
    
    public void sendOutKilled(){
        LOGGER.log(Level.INFO, "sendOutKilled()");
        new Thread(
            new Runnable(){
                @Override
                public void run() {                        
                    if(victims.size() > 0 && killers.size() > 0){
                        LOGGER.log(Level.INFO, "boardcasting out dead player\n" + victims + "\n" + killers);
                        broadcastPlayersKilled(victims, killers);  
                    }  
                    victims.clear();
                    killers.clear();     

                }
            }
        ).start();
    }
    
    public void broadcastPlayersKilled(List <String> victims, List<String> killers){
        LOGGER.log(Level.INFO, victims + "\n" + killers);
        players.forEach(l -> l.getCallback().notifyPlayersKilled(victims, killers));
    }

    public void playerCaughtMonkey(String catcher, String monkey){
        LOGGER.log(Level.INFO, catcher + " caught " + monkey);
        catchers.add(catcher);
        monkeys.add(monkey);
    }
    
    public void sendOutMonkeyInfo(){
        //broadcast out that a monkey got caught
        new Thread(
            new Runnable(){
                @Override
                public void run() {                        
                    if(catchers.size() > 0 && monkeys.size() > 0){
                        broadcastMonkeysCaught(catchers, monkeys);  
                    }  
                    catchers.clear();
                    monkeys.clear();
                }
            }
        ).start();
    }

    public void broadcastMonkeysCaught(List<String> catchers, List<String> monkeys){
        players.forEach(l -> l.getCallback().notifyMonkeysCaught(catchers, monkeys));        
    }
    
    public void gameover(){
        LOGGER.log(Level.SEVERE, "Sending out gameover from server!");
        new Thread(
            new Runnable(){
                @Override
                public void run() {
                    players.forEach(l -> l.getCallback().notifyGameOver());
                }
            }
        ).start();
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
