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
import control.TrapType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionListener;
import network.service.gamestats.PlayerStats;
import network.service.gamestats.TrapEmitter;
import network.service.gamestats.TrapListener;
import network.service.login.Account;
import network.util.ConnectionAttribute;

/**
 *
 * @author truls
 */
public class HostedGameStatsService extends AbstractHostedConnectionService implements TrapEmitter{

    private static final Logger LOGGER = Logger.getLogger(HostedGameStatsService.class);
    
    private static final String GAME_STATS_SERVICE = "game_stats_service";
    
    private final List<TrapListener> trapListeners = new ArrayList<>();
    
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
        GameStatsSessionImpl session = new GameStatsSessionImpl(connection);
        
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
    
    private void notifyTrapListeners(int id, TrapType type){
        for (TrapListener trapListener : trapListeners) {
            trapListener.notifyTrapEvent(id, type);
        }
    }
    
    private void addTrapToWorld(TrapType trap, Vector3f pos){
        
    }

    @Override
    public void addTrapListener(TrapListener trapListener) {
        trapListeners.add(trapListener);
    }
    
    private class GameStatsSessionImpl implements GameStatsSession {
        
        private final HostedConnection connection;
        private GameStatsSessionListener callback;
        private PlayerStats stats;
        
        public GameStatsSessionImpl(HostedConnection connection){
            this.connection = connection;
        }

        @Override
        public void layTrap(TrapType trap, Vector3f pos) {
            Account account = connection.getAttribute(ConnectionAttribute.ACCOUNT);
            if (stats.decreaseTraps(trap) && account != null) { // Player has enough traps left
                addTrapToWorld(trap, pos);
            }
        }
    
    } 
    
}
