/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.chat;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.sun.istack.internal.logging.Logger;
import java.util.logging.Level;
import network.services.login.LoginEvent;
import network.util.ConnectionAttribute;
import network.util.NetConfig;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author truls
 */
public class HostedChatService extends AbstractHostedConnectionService{
    
    private static final Logger LOGGER = Logger.getLogger(HostedChatService.class);
    
    private static final String CHAT = "CHAT";
    
    private RmiHostedService rmiHostedService;
    private int channel;
    
    public HostedChatService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        ChatSpace.initDefualtChatSpaces();
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("ChatHostedService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service started. Client id: {0}", connection.getId());
        NetConfig.networkDelay(100);
        // Retrieve the client side callback
        ChatSessionListener callback = getCallback(connection);
        
        // The newly connected client will be represented by this object on
        // the server side
        ChatSessionImpl player = new ChatSessionImpl(connection, callback);
        
        connection.setAttribute(CHAT, player);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, player, ChatSession.class);
        
        int id = ChatSpace.Chat.GLOBAL.ordinal();
        ChatSpace space = ChatSpace.getChatSpace(id);
        
        space.add(player);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
        ChatSpace.removeFromAll((ChatSessionImpl)connection.getAttribute(CHAT));
    } 
    
    private ChatSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        return NetConfig.getCallback(rmiRegistry, ChatSessionListener.class);
    }
}
