/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.login;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author truls
 */
public class ClientLoginService extends AbstractClientService implements LoginSession{

    private static final Logger LOGGER = Logger.getLogger(ClientLoginService.class);
    
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
            
    public ClientLoginService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public ClientLoginService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if(rmiService == null){
            throw new RuntimeException("ClientLoginService requires RmiService");
        }
        callback = new LoginCallback();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, LoginSessionListener.class);
    }
    
    private LoginSession getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(LoginSession.class);
            if(delegate == null){
                throw new RuntimeException("No remote LoginSession object found");
            }
        }
        return delegate;
    }
    
    @Override
    public void login(String name) {
        getDelegate().login(name);
    }
    
    public void addLoginSessionListener(LoginSessionListener loginSessionListener){
        listeners.add(loginSessionListener);
    }
    
    private class LoginCallback implements LoginSessionListener{
        
        @Override
        public void notifyLogin(boolean loggedIn) {
            LOGGER.log(Level.INFO, "Login result: {0}", loggedIn);
            listeners.forEach(l -> l.notifyLogin(loggedIn));
        }

        @Override
        public void notifyLobbyServerInfo(String hostname, int port) {
            LOGGER.log(Level.INFO, "Lobby server info received. Hostname: {0}, port: {1}",
                    new Object[]{hostname, port});
            listeners.forEach(l -> l.notifyLobbyServerInfo(hostname, port));
        }
    
    }
    
}
