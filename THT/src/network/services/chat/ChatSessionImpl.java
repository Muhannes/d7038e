/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.chat;

import com.jme3.network.HostedConnection;
import network.util.ConnectionAttribute;

/**
 *
 * @author truls
 */
public class ChatSessionImpl implements ChatSession, ChatSessionListener {
    
    private final HostedConnection conn;
    private final ChatSessionListener callback;

    public ChatSessionImpl(HostedConnection conn, ChatSessionListener callback){
        this.conn = conn;
        this.callback = callback;
    }
    
    String getName(){
        return conn.getAttribute(ConnectionAttribute.NAME);
    }

    @Override
    public void sendMessage(String message, int chat) {
        ChatSpace.getChatSpace(chat).postMessage(this, message);
    }

    @Override
    public void joinchat(int chat){
        ChatSpace.getChatSpace(chat).add(this);
    }

    @Override
    public void leavechat(int chat){
        ChatSpace.getChatSpace(chat).remove(this);
    }

    @Override
    public void newMessage(String message, int chat) {
        callback.newMessage(message, chat);
    }

    @Override
    public void playerJoinedChat(String name, int chat) {
        callback.playerJoinedChat(name, chat);
    }

    @Override
    public void playerLeftChat(String name, int chat) {
        callback.playerLeftChat(name, chat);
    }
}
