/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamestats.client;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;
import network.services.gamestats.GameStatsSession;
import network.services.gamestats.GameStatsSessionListener;

/**
 *
 * @author truls
 */
public class ClientGameStatsService extends AbstractClientService{
    private static final Logger LOGGER = Logger.getLogger(ClientGameStatsService.class);
    
    private GameStatsCallback callback = new GameStatsCallback();
    // Used to get notifications from the server
    
    private ArrayList<GameStatsSessionListener> listeners = new ArrayList<>();
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
        
        rmiService.share((byte)channel, callback, GameStatsSessionListener.class);
    }
    
    public void addGameStatsSessionListene(GameStatsSessionListener listener){
        listeners.add(listener);
    }
    
    private class GameStatsCallback implements GameStatsSessionListener {

        @Override
        public void notifyPlayerKilled(String victim, String killer) {
            LOGGER.log(Level.FINE, "Player killed. Victim: {0}, Killer: {1}", new Object[]{victim, killer});
            listeners.forEach(l -> l.notifyPlayerKilled(victim, killer));
        }

        @Override
        public void notifyPlayerEscaped(String name) {
            LOGGER.log(Level.FINE, "Player escaped. Name: {0}", name);
            listeners.forEach(l -> l.notifyPlayerEscaped(name));
        }
    }
}
