/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.chat.client;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.chat.ChatSession;
import network.service.chat.ChatSessionListener;
import network.util.NetConfig;

/**
 * 
 * @author truls
 */
public class ClientChatService extends AbstractClientService implements ChatSession{

    private static final Logger LOGGER = Logger.getLogger(ClientChatService.class.getName());
    
    private ChatSessionListener callback = new ChatSessionCallBack();
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
    
    public void removeChatSessionListener(ChatSessionListener chatSessionListener){
        listeners.remove(chatSessionListener);
    }
    
    @Override
    public void sendMessage(String message, int chat) {
        try {
            getDelegate().sendMessage(message, chat);
        } catch (Exception e) {
            LOGGER.warning("Chat server is offline");
        }
    }
    
    @Override
    public void joinchat(int chat){
        try {
            getDelegate().joinchat(chat);
        } catch (Exception e) {
            LOGGER.warning("Chat server is offline");
        }
    }

    @Override
    public void leavechat(int chat){
        try {
            getDelegate().leavechat(chat);
        } catch (Exception e) {
            LOGGER.warning("Chat server is offline");
        }
    }

    @Override
    public void authenticate(int id, String key, String name) {
        getDelegate().authenticate(id, key, name);
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
            delegate = NetConfig.getDelegate(rmiService, ChatSession.class);
        }
        return delegate;
    }
    
    private class ChatSessionCallBack implements ChatSessionListener{
        
        @Override
        public void newMessage(String message, int chat) {
            LOGGER.log(Level.FINE, "Chat: {0},  Message: {1}", new Object[]{chat, message});
            listeners.forEach(l -> l.newMessage(message, chat));
        }

        @Override
        public void playerJoinedChat(String name, int chat) {
            LOGGER.log(Level.FINE, "Player {0} joined chat: {1}", new Object[]{name, chat});
            listeners.forEach(l -> l.playerJoinedChat(name, chat));
        }

        @Override
        public void playerLeftChat(String name, int chat) {
            LOGGER.log(Level.FINE, "Player {0} left chat: {1}", new Object[]{name, chat});
            listeners.forEach(l -> l.playerLeftChat(name, chat));
        }
        
    }
    
}
