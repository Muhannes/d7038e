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
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionListener;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class ClientGameStatsService extends AbstractClientService implements GameStatsSession{
    private static final Logger LOGGER = Logger.getLogger(ClientGameStatsService.class);
    
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
    public void notifyPlayerKilled(String victim, String killer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyPlayerEscaped(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapPlaced(String trapName, Vector3f newTrap) {
        LOGGER.log(Level.INFO, "new trap " + trapName + " - " + newTrap);
        getDelegate().notifyTrapPlaced(trapName, newTrap);
    }

    @Override
    public void notifyTrapTriggered(String name, String trapName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
        LOGGER.log(Level.INFO, "new trigger that is empty");
    }
    
    private class GameStatsSessionCallback implements GameStatsSessionListener {

        @Override
        public void notifyPlayersKilled(List<String> victims, List<String> killers) {
            LOGGER.log(Level.INFO, "received dead player from server\nVictims"+ victims + "\nKillers" + killers);
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
            LOGGER.log(Level.INFO, "Received new triggeres" + names + " and " + trapNames + "from server");
            if(names.size() > 0 && trapNames.size() > 0){
               listeners.forEach(l -> l.notifyTrapsTriggered(names, trapNames));                
            }
        }
    }
}
