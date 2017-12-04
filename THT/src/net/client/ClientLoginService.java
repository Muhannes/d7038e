/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.List;
import net.LoginSession;
import net.LoginSessionListener;

/**
 *
 * @author truls
 */
public class ClientLoginService extends AbstractClientService implements LoginSession{

    private LoginCallback callback = new LoginCallback();
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
    public boolean login(String name) {
        return getDelegate().login(name);
    }
    
    public void addLoginSessionListener(LoginSessionListener loginSessionListener){
        listeners.add(loginSessionListener);
    }
    
    private class LoginCallback implements LoginSessionListener{

        @Override
        public void notifyLogin(boolean loggedIn) {
            for(LoginSessionListener l : listeners){
                l.notifyLogin(loggedIn);
            }
        }
    
    }
    
}
