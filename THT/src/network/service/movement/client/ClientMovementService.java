/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.client;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.logging.Level;
import com.sun.istack.internal.logging.Logger;
import java.util.List;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class ClientMovementService extends AbstractClientService implements MovementSession{

    private static final Logger LOGGER = Logger.getLogger(ClientMovementService.class);

    private final MovementSessionListener callback = new MovementSessionCallback();

    private final ArrayList<MovementSessionListener> listeners = new ArrayList<>();
            
    private MovementSession delegate;
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?

    public ClientMovementService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;  
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }        
        // Share the callback with the server
        LOGGER.log(Level.SEVERE, "callBack : " + callback);
        rmiService.share((byte)channel, callback, MovementSessionListener.class);
    }
    
    private MovementSession getDelegate(){
        if(delegate == null){
            LOGGER.log(Level.INFO, "Getting delegate from netConfig");
            delegate = NetConfig.getDelegate(rmiService, MovementSession.class);
        }
        return delegate;
    }

    @Override
    public void sendPlayerMovement(PlayerMovement playerMovement) {
        LOGGER.log(Level.INFO, "Sending playerMovement message");
        getDelegate().sendPlayerMovement(playerMovement);
    }
    
    public void addListener(MovementSessionListener newListener){
        listeners.add(newListener);
    }
    
    private class MovementSessionCallback implements MovementSessionListener{

        @Override
        public void notifyPlayerMovement(List<PlayerMovement> playerMovements) { //Are these all the new movements that server broadcasted out?
            LOGGER.info("Movements received from server.\nSize of listeners : " + listeners.size());
            listeners.forEach(l -> l.notifyPlayerMovement(playerMovements)); 
        }

    }

}
