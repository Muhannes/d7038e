/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.sun.istack.internal.logging.Logger;
import java.util.logging.Level;
import network.service.movement.MovementSession;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class HostedMovementService extends AbstractHostedConnectionService {
    
    private static final Logger LOGGER = Logger.getLogger(HostedMovementService.class);

    private static final String MOVEMENT = "MOVEMENT";
    
    private RmiHostedService rmiHostedService;
    private int channel;
    private int playerId;

    public HostedMovementService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        MovementSpace.initDefualtMovementSpaces();
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        
        MovementSpace space = MovementSpace.getMovementSpace(playerId);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("MovementHostedService requires an RMI service.");
        }    
 
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
       LOGGER.log(Level.INFO, "Chat service started. Client id: {0}", connection.getId());
        NetConfig.networkDelay(30);
        
        // The newly connected client will be represented by this object on
        // the server side
        MovementSessionImpl player = new MovementSessionImpl(connection, rmiHostedService);
        
        connection.setAttribute(MOVEMENT, player);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, player, MovementSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
        MovementSpace.removeFromAll((MovementSessionImpl)connection.getAttribute(MOVEMENT));
    }
    
}
