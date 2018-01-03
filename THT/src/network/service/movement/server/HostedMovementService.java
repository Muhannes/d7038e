/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
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
import network.gameserver.GameServer;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionEmitter;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class HostedMovementService extends AbstractHostedConnectionService implements MovementSessionEmitter {
    
    private static final Logger LOGGER = Logger.getLogger(HostedMovementService.class);

    private static final String MOVEMENT = "MOVEMENT";
    
    private RmiHostedService rmiHostedService;
    private final List<MovementSessionImpl> players = new ArrayList<>();
    private final List<MovementSession> movementSessions = new ArrayList<>();
    private final List<PlayerMovement> movements = new ArrayList<>();
    
//    private AssetManager asset;
//    private BulletAppState bulletAppState;
    
    private List<String> updatedPlayers = new ArrayList<>();
//    private MovementSession session;
    private int channel;
    private int playerId;
    
    public HostedMovementService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
//        this.session = session;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        //setAutoHost(false);
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("MovementHostedService requires an RMI service.");
        }
    }
    
    public void playerUpdated(String id){
        if (!updatedPlayers.contains(id)) {
            updatedPlayers.add(id);
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
    
    /**
     * Sends out updates to all players
     * TODO: Filter based on location, i.e. only send to those that need the info
     * @param movements 
     */
    public void broadcast(List<PlayerMovement> movements){
        players.forEach(p -> p.getCallback().newMessage(movements));        
    }
    
    public void sendOutMovements(Node playersNode){
        //Send out movements everything 10ms 
        new Thread(
            new Runnable(){
                @Override
                public void run() {
                    while(true){
                        try {                    
                            Thread.sleep(20);                    
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            for(String id : updatedPlayers){
                                Vector3f location = new Vector3f(playersNode.getChild(id).getLocalTranslation());
                                Vector3f direction = new Vector3f(playersNode.getChild(id).getControl(BetterCharacterControl.class).getWalkDirection());
                                Vector3f rotation = new Vector3f(playersNode.getChild(id).getControl(BetterCharacterControl.class).getViewDirection());

                                //do same for location
                                PlayerMovement pm = new PlayerMovement(id, location, direction, rotation);
                                movements.add(pm);
                            }
                            if (!movements.isEmpty()) {
                                broadcast(movements);
                                //Clear movements
                                movements.clear();
                                updatedPlayers.clear(); //changed from within the loop.           
                            }
                        }
                    }
                }            
            }
        ).start();        
    }

    @Override
    public void addSessions(MovementSession movementSession) {
        movementSessions.add(movementSession);
    }

    @Override
    public void removeSessions(MovementSession movementSession) {
        movementSessions.remove(movementSession);
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
            movementSessions.forEach(l -> l.sendMessage(playerMovement));
        }
        
    }
}
