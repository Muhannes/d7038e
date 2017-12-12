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
import java.util.List;
import network.services.chat.ClientChatService;
import utils.eventbus.EventBus;

/**
 *
 * @author hannes
 */
public class ClientGameSetupService extends AbstractClientService implements GameSetupSession{

    private static final Logger LOGGER = Logger.getLogger(ClientGameSetupService.class);
    
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
        GameSetupSessionListener callback = new GameSetupSessionListenerImpl();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, GameSetupSessionListener.class);
    }
    
    private GameSetupSession getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(GameSetupSession.class);
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
    
    private class GameSetupSessionListenerImpl implements GameSetupSessionListener {

        @Override
        public void initPlayer(List<Player> p) {
            LOGGER.fine("InitPlayer Received!");
            EventBus.publish(new PlayerInitEvent(p), PlayerInitEvent.class);
        }

        @Override
        public void startGame() {
            EventBus.publish(new StartGameEvent(), StartGameEvent.class);
        }
        
    }
    
}
