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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.service.login.Account;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class HostedMovementService extends AbstractHostedConnectionService {
    
    private static final Logger LOGGER = Logger.getLogger(HostedMovementService.class);

    private static final String MOVEMENT = "MOVEMENT";
    
    private RmiHostedService rmiHostedService;
    private List<MovementSessionImpl> players = new ArrayList<>();
    private List<PlayerMovement> movements = new ArrayList<>();
    
    private MovementSession session;
    private int channel;
    private int playerId;
    
    public HostedMovementService(MovementSession session){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        this.session = session;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
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
        MovementSessionImpl player = new MovementSessionImpl(connection);
        players.add(player);
        
        connection.setAttribute(MOVEMENT, player);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmiHostedService.shareGlobal(session, MovementSession.class);
        //rmi.share((byte)channel, player, MovementSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
    }
    
    private void broadcast(List<PlayerMovement> movements){
        players.forEach(p -> p.newMessage(movements));
    }
    
    private class MovementSessionImpl implements MovementSessionListener{

        private final HostedConnection conn;
        private MovementSessionListener callback;

        MovementSessionImpl(HostedConnection conn){
            this.conn = conn;
        }

        private MovementSessionListener getCallback(){
            if (callback == null){
                RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(conn);
                callback =  NetConfig.getCallback(rmiRegistry, MovementSessionListener.class);
            }
            return callback;
        }

        @Override
        public void newMessage(List<PlayerMovement> playerMovements) {
            getCallback().newMessage(playerMovements);
        }
    }
}
