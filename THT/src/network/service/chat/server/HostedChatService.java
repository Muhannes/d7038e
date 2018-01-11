/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.chat.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.chat.ChatSession;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class HostedChatService extends AbstractHostedConnectionService{
    
    private static final Logger LOGGER = Logger.getLogger(HostedChatService.class.getName());
    
    private static final String CHAT = "CHAT";
    
    private RmiHostedService rmiHostedService;
    private int channel;
    
    public HostedChatService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        ChatSpace.initDefualtChatSpaces();
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        //setAutoHost(false);
        
        int id = ChatSpace.Chat.GLOBAL.ordinal();
        ChatSpace space = ChatSpace.getChatSpace(id);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("ChatHostedService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service started. Client id: {0}", connection.getId());
        NetConfig.networkDelay(30);
        
        // The newly connected client will be represented by this object on
        // the server side
        ChatSessionImpl player = new ChatSessionImpl(connection, rmiHostedService);
        
        connection.setAttribute(CHAT, player);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, player, ChatSession.class);
        /*
        int id = ChatSpace.Chat.GLOBAL.ordinal();
        ChatSpace space = ChatSpace.getChatSpace(id);
        
        space.add(player);
        */
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
        ChatSpace.removeFromAll((ChatSessionImpl)connection.getAttribute(CHAT));
    } 
    
}
