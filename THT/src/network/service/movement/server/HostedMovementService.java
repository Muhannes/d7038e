/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.scene.Node;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.GameServer;
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
    
    private Node playersNode;
    
    private List<String> updateMovements = new ArrayList<>();
//    private MovementSession session;
    private int channel;
    private int playerId;
    
    public HostedMovementService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
//        this.session = session;
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
        //rmiHostedService.shareGlobal(session, MovementSession.class);
        rmi.share((byte)channel, player, MovementSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
    }
    
    public void broadcast(List<PlayerMovement> movements){
        //players.forEach(p -> p.newMessage(movements));
    }
    
    public void sendOutMovements(){
        //Send out movements everything 10ms 
        LOGGER.log(Level.INFO, "Sending out to clients");                    
        
        new Runnable(){
            @Override
            public void run() {
                while(true){
                    try {                    
                        Thread.sleep(100);                    
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        //TODO: Fetch info from tree (only for the id)
                        //Create PlayerMovements
                        //Send out to clients
                        for(String id : updateMovements){
                            Vector3f direction = new Vector3f(playersNode.getChild(id).getControl(CharacterControl.class).getWalkDirection());
                            Quaternion rotation = new Quaternion(playersNode.getChild(id).getLocalRotation());
                            //do same for location
                            PlayerMovement pm = new PlayerMovement(id, direction, rotation);
                            movements.add(pm);
                        }        
                        broadcast(movements);
                        //TODO: Clear movements
                    }                    
                }
            }            
        }.run();
        
    }
    
    private class MovementSessionImpl implements MovementSession{

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
        public void sendMessage(PlayerMovement playerMovement) {
            System.out.println("Receiving playermovement in GameServer");
            playersNode.getChild(playerMovement.id).getControl(CharacterControl.class).setWalkDirection(playerMovement.direction);
            playersNode.getChild(playerMovement.id).setLocalRotation(playerMovement.rotation);
            //TODO location
            updateMovements.add(playerMovement.id);        
        }

        
    }
}
