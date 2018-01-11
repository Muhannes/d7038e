/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.login.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.lobby.server.HostedLobbyService;
import network.service.login.LoginSession;
import network.service.login.LoginSessionListener;
import network.util.NetConfig;

/**
 *
 * @author truls
 */
public class HostedLoginService extends AbstractHostedConnectionService{
    
    private static final Logger LOGGER = Logger.getLogger(HostedLoginService.class.getName());
    
    private final List<HostedConnection> loginListeners = new ArrayList<>();
    
    private RmiHostedService rmiService;
    // Used to sync with client and send data
    
    private int channel;
    // Channel we send on, is it a port though?
    
    private MessageDigest digest;
    
    public HostedLoginService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedLoginService(int channel){
        this.channel = channel;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        LOGGER.setLevel(Level.INFO);
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(HostedLobbyService.class.getName()).log(Level.SEVERE, null, ex);
        }
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("HostedLoginService requires an RMI service.");
        }      
    }

    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Login service started. Client id: {0}", connection.getId());
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        NetConfig.networkDelay(50);
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
        LOGGER.log(Level.INFO, "Login service stopped. Client id: {0}", connection.getId());
    }
    
    
    private LoginSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        return NetConfig.getCallback(rmi, LoginSessionListener.class);
    }
    
    private class LoginSessionImpl implements LoginSession{

        private HostedConnection connection;
        // Connection to a client
        
        public LoginSessionImpl(HostedConnection connection){
            this.connection = connection;
        }
        
        @Override
        public void login(String name) {
            LOGGER.log(Level.INFO, "Login request received from connection with ID: {0} and name: {1}", 
                    new Object[]{connection.getId(), name});

            byte[] hash = digest.digest("".getBytes());
            for (HostedConnection loginListener : loginListeners) {
                getCallback(loginListener).notifyLogin(true, hash.toString(), connection.getId(), name);
            }
            getCallback(connection).notifyLogin(true, hash.toString(), connection.getId(), name);
            
            LOGGER.info("Login callbacks sent out. " + loginListeners.size() + " listeners.");
            // NOTE: Moved login ack to lobbySession (Due to client trying to access LobbyManager before shared)
        }

        @Override
        public void listenForLogins() {
            loginListeners.add(connection);
            LOGGER.info("Login listeners: " + loginListeners.size());
        }
    }
    
}
