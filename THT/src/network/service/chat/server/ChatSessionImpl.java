/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.chat.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import network.service.chat.ChatSession;
import network.service.chat.ChatSessionListener;
import network.service.login.Account;
import network.service.login.LoginListenerService;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class ChatSessionImpl implements ChatSession, ChatSessionListener {
    
    private final HostedConnection conn;
    private ChatSessionListener callback;
    private final RmiHostedService rmi;
    private Account account;
    private boolean authenticated;
    
    public ChatSessionImpl(HostedConnection conn, RmiHostedService rmi){
        this.conn = conn;
        this.rmi = rmi;
    }
    
    String getName(){
        if (!authenticated) {
            return null;
        }
        return account.name;
    }

    @Override
    public void sendMessage(String message, int chat) {
        if (!authenticated) {
            return ;
        }
        ChatSpace.getChatSpace(chat).postMessage(this, message);
    }

    @Override
    public void joinchat(int chat){
        if (!authenticated) {
            return;
        }
        ChatSpace.getChatSpace(chat).add(this);
    }

    @Override
    public void leavechat(int chat){
        if (!authenticated) {
            return;
        }
        ChatSpace.getChatSpace(chat).remove(this);
    }

    @Override
    public void newMessage(String message, int chat) {
        if (!authenticated) {
            return;
        }
        getCallback().newMessage(message, chat);
    }

    @Override
    public void playerJoinedChat(String name, int chat) {
        if (!authenticated) {
            return;
        }
        getCallback().playerJoinedChat(name, chat);
    }

    @Override
    public void playerLeftChat(String name, int chat) {
        if (!authenticated) {
            return;
        }
        getCallback().playerLeftChat(name, chat);
    }
    
    private ChatSessionListener getCallback(){
        if (callback == null){
            RmiRegistry rmiRegistry = rmi.getRmiRegistry(conn);
            callback =  NetConfig.getCallback(rmiRegistry, ChatSessionListener.class);
        }
        return callback;
    }

    @Override
    public void authenticate(int id, String key, String name) {
        for (Account account : LoginListenerService.getAccounts()) {
            if (account.isEqual(id, key)) {
                this.authenticated = true;
                this.account = account;
            }
        }
    }
    
}
