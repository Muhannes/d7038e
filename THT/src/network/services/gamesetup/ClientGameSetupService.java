/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import api.models.Player;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import network.util.NetConfig;

/**
 *
 * @author hannes
 */
public class ClientGameSetupService extends AbstractClientService implements GameSetupSession{

    private static final Logger LOGGER = Logger.getLogger(ClientGameSetupService.class.getName());
    
    private List<GameSetupSessionListener> listeners = new ArrayList<>();
    
    private GameSetupSessionListener callback;
    
    private GameSetupSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public ClientGameSetupService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }

    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ClientGameSetupService requires RMI service");
        }
        callback = new GameSetupSessionListenerImpl();
        rmiService.share((byte)channel, callback, GameSetupSessionListener.class);
    }
    
    private GameSetupSession getDelegate(){
        if(delegate == null){
            delegate = NetConfig.getDelegate(rmiService, GameSetupSession.class);
        }
        return delegate;
    }
    
    @Override
    public void join(int globalID, String key, String name) {
        getDelegate().join(globalID, key, name);
    }

    @Override
    public void ready() {
        getDelegate().ready();
    }
    
    public void addGameSetupSessionListener(GameSetupSessionListener listener){
        listeners.add(listener);
    }
    
    public void removeGameSetupSessionListener(GameSetupSessionListener listener){
        listeners.remove(listener);
    }
    
    private class GameSetupSessionListenerImpl implements GameSetupSessionListener {

        @Override
        public void initPlayer(List<Player> p) {
            listeners.forEach(l -> l.initPlayer(p));
        }

        @Override
        public void startGame() {
            listeners.forEach(l -> l.startGame());
        }
        
    }
    
}
