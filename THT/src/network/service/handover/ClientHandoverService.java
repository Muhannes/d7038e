/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

import network.service.gamesetup.PlayerInfoEvent;
import com.jme3.network.Client;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.Map;
import network.util.NetConfig;
import utils.eventbus.EventBus;

/**
 *
 * @author hannes
 */
public class ClientHandoverService extends AbstractClientService {

    
    private HandoverSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    
    public ClientHandoverService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("HandoverClientService requires RMI service");
        }
        HandoverSessionListener callback = new GameLobbySessionListenerImpl();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, HandoverSessionListener.class);
        
        
    }
    
    public void joinLobby(){
        getDelegate().join(-1, NetConfig.GAME_SERVER_PORT);
    }
    
    private HandoverSession getDelegate(){
        if(delegate == null){
            delegate = NetConfig.getDelegate(rmiService, HandoverSession.class);
        }
        return delegate;
    }
    
    
    private class GameLobbySessionListenerImpl implements HandoverSessionListener {

        @Override
        public void startSetup(Map<Integer, String> playerInfo) {
            EventBus.publish(new PlayerInfoEvent(playerInfo), PlayerInfoEvent.class);
            new Thread(new Runnable() { // Is this needed to be run in different thread? weird error if not when tried earlier
                @Override
                public void run() {
                    Client client = getClient();
                    if (client.isConnected()) {
                        client.close();
                    }
                    
                }
            }).start();
        }
        
    }
    
}
