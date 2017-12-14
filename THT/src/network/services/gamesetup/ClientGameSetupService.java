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
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import network.services.chat.ClientChatService;
import utils.eventbus.EventBus;

/**
 *
 * @author hannes
 */
public class ClientGameSetupService extends AbstractClientService implements GameSetupSession{

    private static final Logger LOGGER = Logger.getLogger(ClientGameSetupService.class);
    
    private List<GameSetupSessionListener> listeners = new ArrayList<>();
    
    private GameSetupSessionListenerImpl callback;
    
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
        LOGGER.fine("Init ClientGameSetup");
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }
        
        callback = new GameSetupSessionListenerImpl();
        System.out.println("Callback : " + callback);
        // Share the callback with the server
        rmiService.share((byte)channel, callback, GameSetupSessionListener.class);
    }
    
    private GameSetupSession getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(GameSetupSession.class);
            System.out.println("GameSetupSession (Delegate) : " + delegate);
            if( delegate == null ) {
                throw new RuntimeException("No GameSetup session found");
            } 
        }
        return delegate;
    }

    @Override
    public void join(int globalID) {
        getDelegate().join(globalID);
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
            LOGGER.fine("InitPlayer Received!");
            //EventBus.publish(new PlayerInitEvent(p), PlayerInitEvent.class);
            listeners.forEach(l -> l.initPlayer(p));
        }

        @Override
        public void startGame() {
            System.out.println("Sending out startGame! before");
            //EventBus.publish(new StartGameEvent(), StartGameEvent.class);
            listeners.forEach(l -> l.startGame());
            System.out.println("Sending out startGame! after");
            
        }
        
    }
    
}
