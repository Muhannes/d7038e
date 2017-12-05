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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class HostedChatService extends AbstractHostedConnectionService{
    
    private RmiHostedService rmiHostedService;
    private int channel;
    
    private List<ChatSessionImpl> players = new ArrayList<>();
    
    public HostedChatService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("ChatHostedService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        // The newly connected client will be represented by this object on
        // the server side
        ChatSessionImpl player = new ChatSessionImpl(connection);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, player, ChatSession.class);
        
        for(ChatSessionImpl p: players){
            p.playerJoined("" + connection.getId());
        }
        
        players.add(player);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection hc) {
        // Nothing
        for (ChatSessionImpl player : players) {
            if (player.conn.equals(hc)) {
                players.remove(player);
                return;
            }
        }
    }
    
    /**
     * Post a message from one player to all others
     * @param from From Player
     * @param message Message to be sent
     */
    private void postMessage(ChatSessionImpl from, String message){
        System.out.println("Chat: " + from.toString() + " said " + message);
        for(ChatSessionImpl player : players){
            player.newMessage(message);
        }
    }
    
    private class ChatSessionImpl implements ChatSession, ChatSessionListener{

        private HostedConnection conn;
        private ChatSessionListener callback;
        
        public ChatSessionImpl(HostedConnection conn){
            this.conn = conn;
        }
        
        protected ChatSessionListener getCallback(){
            if(callback == null){
                RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(conn);
                callback = rmiRegistry.getRemoteObject(ChatSessionListener.class);
                if( callback == null){ 
                    throw new RuntimeException("Unable to locate client callback for ChatSessionListener");
                }
            }
            return callback;
        }
        
        @Override
        public void sendMessage(String message) {
            postMessage(this, message);
        }

        @Override
        public void newMessage(String message) {
            getCallback().newMessage(message);
        }

        @Override
        public void playerJoined(String name) {
            getCallback().playerJoined(name);
        }

        @Override
        public void playerLeft(String name) {
            getCallback().playerLeft(name);
        }
    
    }
    
}
