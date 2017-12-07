/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.chat;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author truls
 */
public class ClientChatService extends AbstractClientService implements ChatSession{

    private static final Logger LOGGER = Logger.getLogger(ClientChatService.class);
    
    private ChatSessionCallBack callback = new ChatSessionCallBack();
    // Used to get notifications from the server
    
    private ArrayList<ChatSessionListener> listeners = new ArrayList<>();
    // Used to notify listeners on client side
    
    private ChatSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public ClientChatService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }
    
    public void addChatSessionListener(ChatSessionListener chatSessionListener){
        listeners.add(chatSessionListener);
    }
    
    @Override
    public void sendMessage(String message) {
        getDelegate().sendMessage(message);
    }

    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }
        
        // Share the callback with the server
        rmiService.share((byte)channel, callback, ChatSessionListener.class);
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
    
    private class ChatSessionCallBack implements ChatSessionListener{
        
        @Override
        public void newMessage(String message) {
            LOGGER.log(Level.FINE, "Message: {0}", message);
            for(ChatSessionListener l : listeners){
                l.newMessage(message);
            }
        }

        @Override
        public void playerJoinedChat(String name) {
            LOGGER.log(Level.FINE, "Player joined chat: {0}", name);
            for(ChatSessionListener l : listeners){
                l.playerJoinedChat(name);
            }
        }

        @Override
        public void playerLeftChat(String name) {
            LOGGER.log(Level.FINE, "Player left chat: {0}", name);
            for(ChatSessionListener l : listeners){
                l.playerLeftChat(name);
            }
        }
        
    }
    
}
