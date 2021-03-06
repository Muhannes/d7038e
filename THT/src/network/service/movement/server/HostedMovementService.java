/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.MonkeyNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.gameserver.Filter;
import network.service.login.Account;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionEmitter;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.util.ConnectionAttribute;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class HostedMovementService extends AbstractHostedConnectionService implements MovementSessionEmitter {
    
    private static final Logger LOGGER = Logger.getLogger(HostedMovementService.class.getName());

    private static final String MOVEMENT = "MOVEMENT";
    private static int movementCounter = 0;
    
    private RmiHostedService rmiHostedService;
    private final List<MovementSessionImpl> players = new ArrayList<>();
    private final List<MovementSession> movementSessions = new ArrayList<>();
    private final List<PlayerMovement> movements = new ArrayList<>();
    
//    private AssetManager asset;
//    private BulletAppState bulletAppState;
    
    private CopyOnWriteArrayList<String> updatedPlayers = new CopyOnWriteArrayList<>();
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
    
    public void clear(){
        updatedPlayers.clear();
        movements.clear();
        players.clear();
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
    private synchronized void broadcast(MovementSessionImpl player, List<PlayerMovement> movements){
        player.getCallback().notifyPlayerMovement(movements);
    }
    
    public void broadcastEverything(Node playersNode){
        List<PlayerMovement> movements = new ArrayList<>();
        
        for(Spatial s : playersNode.getChildren()){
            if (!(s instanceof MonkeyNode)) {
                Vector3f location = new Vector3f(s.getLocalTranslation());
                Vector3f direction = new Vector3f(s.getControl(CharacterControl.class).getWalkDirection());
                Vector3f rotation = new Vector3f(s.getControl(CharacterControl.class).getViewDirection());

                //do same for location
                PlayerMovement pm = new PlayerMovement(s.getName(), location, direction, rotation);
                movements.add(pm);
            }
        }
        
        players.forEach(p -> broadcast(p, movements));
    }
    
    public Runnable getMovementSender(Node playersNode, Node rooms){
        //Send out movements everything 10ms 
        Runnable r = () -> {
            movementCounter++;
            if(movementCounter == 50){
                broadcastEverything(playersNode);
                movementCounter = 0;
            }else{
                List<String> ups = new ArrayList<>(updatedPlayers);
                updatedPlayers.clear(); //changed from within the loop.
                for(String id : ups){
                    Spatial s = playersNode.getChild(id);
                    if (s != null) {
                        Vector3f location = new Vector3f(s.getLocalTranslation());
                        CharacterControl cc = s.getControl(CharacterControl.class);
                        Vector3f direction = new Vector3f(cc.getWalkDirection());
                        Vector3f rotation = new Vector3f(cc.getViewDirection());
                        //do same for location
                        PlayerMovement pm = new PlayerMovement(id, location, direction, rotation);
                        movements.add(pm);
                    } else {
                        LOGGER.info("Spatial was null when trying to send info");
                    }

                }
           
                if (!movements.isEmpty()) {
                    // For each session, filter out the player movements that this session is interested in.
                    players.forEach((session) -> {
                        Account account = session.conn.getAttribute(ConnectionAttribute.ACCOUNT);
                        String id = "" + account.id;
                        List<PlayerMovement> filtered = Filter.getPlayerMovements(
                                playersNode.getChild(id), movements, rooms);
                        if(filtered.size() > 0){
                            broadcast(session, filtered);
                        }
                    });
                    movements.clear();
                }
            }
        };
        return r;
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
        public void sendPlayerMovement(PlayerMovement playerMovement) {
            //LOGGER.log(Level.INFO, "server received new movement message\nid : " + playerMovement.id + " direction : " + playerMovement.direction);
            movementSessions.forEach(l -> l.sendPlayerMovement(playerMovement));
        }
        
    }
    
}
