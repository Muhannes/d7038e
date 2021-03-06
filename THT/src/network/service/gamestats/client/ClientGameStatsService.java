/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats.client;

import com.jme3.math.Vector3f;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionListener;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class ClientGameStatsService extends AbstractClientService implements GameStatsSession{
    private static final Logger LOGGER = Logger.getLogger(ClientGameStatsService.class.getName());
    
    private final GameStatsSessionListener callback = new GameStatsSessionCallback();
    // Used to get notifications from the server
    
    private final ArrayList<GameStatsSessionListener> listeners = new ArrayList<>();
    // Used to notify listeners on client side
    
    private GameStatsSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public ClientGameStatsService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public ClientGameStatsService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }
        LOGGER.log(Level.SEVERE, "callBack : " + callback);
        rmiService.share((byte)channel, callback, GameStatsSessionListener.class);
    }
    
    public void addGameStatsSessionListener(GameStatsSessionListener listener){
        listeners.add(listener);
    }
    
    private GameStatsSession getDelegate(){
        if(delegate == null){
            LOGGER.log(Level.INFO, "Getting delegate from netConfig");
            delegate = NetConfig.getDelegate(rmiService, GameStatsSession.class);
        }
        return delegate;
    }
    
    @Override
    public void notifyTrapPlaced(String trapName, Vector3f newTrap) {
        getDelegate().notifyTrapPlaced(trapName, newTrap);
    }

    @Override
    public void notifyJump(String player) {
        getDelegate().notifyJump(player);
    }

    @Override
    public void notifySlash(String player) {
        getDelegate().notifySlash(player);
    }
    
    private class GameStatsSessionCallback implements GameStatsSessionListener {
        
        @Override
        public void notifyPlayersKilled(String victim, String killer) {
            listeners.forEach(l -> l.notifyPlayersKilled(victim, killer));
        }

        @Override
        public void notifyTrapsPlaced(String trapName, Vector3f newTrap) {
            listeners.forEach(l -> l.notifyTrapsPlaced(trapName, newTrap));
        }

        @Override
        public void notifyTrapsTriggered(String name, String trapName) {
            listeners.forEach(l -> l.notifyTrapsTriggered(name, trapName));                
        }

        @Override
        public void notifyMonkeysCaught(String catcher, String monkey) {
            listeners.forEach(l -> l.notifyMonkeysCaught(catcher, monkey));
        }

        @Override
        public void notifyGameOver(String winners) {
            listeners.forEach(l -> l.notifyGameOver(winners));
        }

        @Override
        public void notifyPlayerJumped(String player) {
            listeners.forEach(l -> l.notifyPlayerJumped(player));
        }

        @Override
        public void notifyPlayerSlashed(String player) {
            listeners.forEach(l -> l.notifyPlayerSlashed(player));
        }
    }
}
