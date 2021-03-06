/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.login;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import network.util.NetConfig;

/**
 *
 * @author hannes
 */
public class LoginListenerService extends AbstractClientService{

    private static final Logger LOGGER = Logger.getLogger(LoginListenerService.class.getName());
    
    private static final List<Account> accounts = new ArrayList<>();
    
    private LoginCallback callback;
    // Used to get notifications from the server
    
    private List<LoginSessionListener> listeners = new ArrayList<>();
    // Used to notify listeners on client side
    
    private LoginSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
            
    public LoginListenerService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public LoginListenerService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if(rmiService == null){
            throw new RuntimeException("LobbyLoginService requires RmiService");
        }
        callback = new LoginCallback();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, LoginSessionListener.class);
    }
    
    private LoginSession getDelegate(){
        if (delegate == null){
            delegate = NetConfig.getDelegate(rmiService, LoginSession.class);
        }
        return delegate;
    }
    
    public void listenForLogins(){
        getDelegate().listenForLogins();
    }
    
    public static List<Account> getAccounts(){
        return accounts;
    }
    
    public void addLoginSessionListener(LoginSessionListener loginSessionListener){
        listeners.add(loginSessionListener);
    }
    
    public void removeLoginSessionListener(LoginSessionListener loginSessionListener){
        listeners.remove(loginSessionListener);
    }
    
    private class LoginCallback implements LoginSessionListener{
        
        @Override
        public void notifyLogin(boolean loggedIn, String key, int id, String name) {
            if (loggedIn) {
                LOGGER.info("New Account added: " + name);
                accounts.add(new Account(name, id, key));
            }
        }

        @Override
        public void notifyLobbyServerInfo(String hostname, int port) {
            // DO nothing!
        }
    
    }
    
}

