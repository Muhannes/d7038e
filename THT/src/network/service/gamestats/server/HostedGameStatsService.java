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
import com.sun.istack.internal.logging.Logger;
import java.util.logging.Level;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionListener;

/**
 *
 * @author truls
 */
public class HostedGameStatsService extends AbstractHostedConnectionService{

    private static final Logger LOGGER = Logger.getLogger(HostedGameStatsService.class);
    
    private static final String GAME_STATS_SERVICE = "game_stats_service";
    
    private RmiHostedService rmiHostedService;
    
    private int channel;
    
    public HostedGameStatsService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedGameStatsService(int channel){
        this.channel = channel;
    }   

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("GameStats service requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service started. Client id: {0}", connection.getId());
        
        // Retrieve the client side callback
        GameStatsSessionListener callback = getCallback(connection);
        
        // The newly connected client will be represented by this object on
        // the server side
        GameStatsSessionImpl session = new GameStatsSessionImpl(connection, callback);
        
        connection.setAttribute(GAME_STATS_SERVICE, session);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, GameStatsSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service stopped: Client id: {0}", connection.getId());
    }
    
    private GameStatsSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        GameStatsSessionListener callback = rmiRegistry.getRemoteObject(GameStatsSessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for ChatSessionListener");
        }
        return callback;
    }
    
    private class GameStatsSessionImpl implements GameStatsSession, GameStatsSessionListener {
        
        private HostedConnection connection;
        private GameStatsSessionListener callback;
        
        public GameStatsSessionImpl(HostedConnection connection, GameStatsSessionListener callback){
            this.connection = connection;
            this.callback = callback;
        }

        @Override
        public void notifyPlayerKilled(String victim, String killer) {
            callback.notifyPlayerKilled(victim, killer);
        }

        @Override
        public void notifyPlayerEscaped(String name) {
            callback.notifyPlayerEscaped(name);
        }

        @Override
        public void notifyTrapPlaced(String id, Vector3f newTrap) {
            callback.notifyTrapPlaced(id, newTrap);
        }

        @Override
        public void notifyTrapTriggered(String id) {
            callback.notifyTrapTriggered(id);
        }
    
    } 
    
}
