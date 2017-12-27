/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.network.MessageConnection;
import com.sun.istack.internal.logging.Logger;
import network.util.NetConfig;
import network.service.lobby.LobbySession;

/**
 *
 * @author truls
 */
public class HostedLobbyService extends AbstractHostedConnectionService{
    
    private static final Logger LOGGER = Logger.getLogger(HostedLobbyService.class);
    
    private static final String LOBBY_SERVICE = "lobby_service";
    
    private RmiHostedService rmiService;
    // Used to sync with client and send data
        
    private int channel;
    // Channel we send on, is it a port though?
    
    public HostedLobbyService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedLobbyService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("LobbyService requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        NetConfig.networkDelay(30);
        
        LobbySessionImpl session = new LobbySessionImpl(connection, rmiService);
        connection.setAttribute(LOBBY_SERVICE, session);
        
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, LobbySession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LobbySessionImpl lobbyManagerImpl = connection.getAttribute(LOBBY_SERVICE);
        lobbyManagerImpl.leave();
    }
    
}
