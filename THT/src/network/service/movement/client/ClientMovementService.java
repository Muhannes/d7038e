/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.client;

import com.jme3.math.Vector3f;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.util.ArrayList;
import java.util.logging.Level;
import com.sun.istack.internal.logging.Logger;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class ClientMovementService extends AbstractClientService implements MovementSession{

    private static final Logger LOGGER = Logger.getLogger(ClientMovementService.class);

    private MovementSessionListener callback = new MovementSessionCallback();

    private ArrayList<MovementSessionListener> listeners = new ArrayList<>();
            
    private MovementSession delegate;
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?

    ClientMovementService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;  
    }
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("ChatClientService requires RMI service");
        }        
        // Share the callback with the server
        rmiService.share((byte)channel, callback, MovementSessionListener.class);
    }
    
    @Override
    public void sendMessage(Vector3f location, int id) {
        try {
            getDelegate().sendMessage(location, id);
        } catch (Exception e) {
            LOGGER.warning("Movement server is offline");
        }
    }
    
        private MovementSession getDelegate(){
        if(delegate == null){
            delegate = NetConfig.getDelegate(rmiService, MovementSession.class);
        }
        return delegate;
    }

    @Override
    public void joinedMovement(int id) {
        try {
            getDelegate().joinedMovement(id);
        } catch (Exception e) {
            LOGGER.warning("Chat server is offline");
        }
    }

    @Override
    public void leftMovement(int id) {
        try {
            getDelegate().leftMovement(id);
        } catch (Exception e) {
            LOGGER.warning("Chat server is offline");
        }
    }

    private class MovementSessionCallback implements MovementSessionListener{

        @Override
        public void newMessage(Vector3f location, int id) {
            LOGGER.log(Level.FINE, "id: {0},  Location: {1}", new Object[]{id, location});
            listeners.forEach(l -> l.newMessage(location, id));
        }

        @Override
        public void playerJoinedMovement(String name, int id) {
            LOGGER.log(Level.FINE, "Player {0} joined game: {1}", new Object[]{name, id});
            listeners.forEach(l -> l.playerJoinedMovement(name, id));
        }

        @Override
        public void playerLeftMovement(String name, int id) {
            LOGGER.log(Level.FINE, "Player {0} left game: {1}", new Object[]{name, id});
            listeners.forEach(l -> l.playerLeftMovement(name, id));
        }

    }

}
