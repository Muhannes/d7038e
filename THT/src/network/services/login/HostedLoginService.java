/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.login;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import network.util.ConnectionAttribute;
import utils.eventbus.EventBus;

/**
 *
 * @author truls
 */
public class HostedLoginService extends AbstractHostedConnectionService{
    
    private RmiHostedService rmiService;
    // Used to sync with client and send data
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public HostedLoginService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedLoginService(int channel){
        this.channel = channel;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("HostedLoginService requires an RMI service.");
        }      
    }

    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        System.out.println("HostedLoginService: New connection with ID = " + connection.getId());
        
        // The newly connected client will be represented by this object on
        // the server side
        LoginSessionImpl session = new LoginSessionImpl(connection);
        
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        if (rmi == null) {
            throw new Error("RMI is null!");
        }
        rmi.share((byte)channel, session, LoginSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        System.out.println("Connection ended with ID = " + connection.getId());
        // Nothing
    }
    
    private class LoginSessionImpl implements LoginSession{

        private HostedConnection connection;
        // Connection to a client
        
        private LoginSessionListener callback;
        // Used to communicate with client side
        
        public LoginSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void login(String name) {
            System.out.println("Login request received from connection with ID = " + connection.getId());
            connection.setAttribute(ConnectionAttribute.NAME, name);
            getCallback().notifyLogin(true);
            EventBus.publish(new LoginEvent(connection), LoginEvent.class);
        }
        
        private LoginSessionListener getCallback(){
            if(callback == null){
                RmiRegistry rmi = rmiService.getRmiRegistry(connection);
                callback = rmi.getRemoteObject(LoginSessionListener.class);
                if(callback == null){
                    throw new RuntimeException("No client callback found for LoginSessionListener");
                }
            }
            return callback;
        }
    }
    
}
