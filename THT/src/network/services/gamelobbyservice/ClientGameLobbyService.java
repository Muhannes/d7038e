/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamelobbyservice;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.Map;
import network.services.chat.ChatSession;

/**
 *
 * @author hannes
 */
public class ClientGameLobbyService extends AbstractClientService{

    
    private ChatSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    
    public ClientGameLobbyService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }
        GameLobbySessionListener callback = new GameLobbySessionListenerImpl();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, GameLobbySessionListener.class);
    }
    
    private ChatSession getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(ChatSession.class);
            if( delegate == null ) {
                throw new RuntimeException("No chat session found");
            } 
        }
        return delegate;
    }
    
    
    private class GameLobbySessionListenerImpl implements GameLobbySessionListener {

        @Override
        public void startSetup(Map<Integer, String> playerInfo) {
            // todo : setup, bla blabla
        }
        
    }
    
}
