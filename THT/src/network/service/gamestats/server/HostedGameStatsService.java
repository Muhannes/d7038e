/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats.server;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.sun.istack.internal.logging.Logger;
import control.trap.FreezeTrap;
import control.trap.Trap;
import control.trap.TrapType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.GameStatsSessionListener;
import network.service.gamestats.PlayerStats;
import network.service.gamestats.TrapListener;
import network.service.login.Account;
import network.util.ConnectionAttribute;

/**
 *
 * @author truls
 */
public class HostedGameStatsService extends AbstractHostedConnectionService implements PhysicsCollisionListener{

    private static final Logger LOGGER = Logger.getLogger(HostedGameStatsService.class);
    
    private static final String GAME_STATS_SERVICE = "game_stats_service";
    
    private RmiHostedService rmiHostedService;
    
    private final int channel;
    private final Node trapNode;
    private final BulletAppState bulletAppState;
    
    public HostedGameStatsService(int channel, Node trapNode, BulletAppState bulletAppState){
        this.channel = channel;
        this.trapNode = trapNode;
        this.bulletAppState = bulletAppState;
        this.bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }   

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        setAutoHost(false);
        
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("GameStats service requires an RMI service.");
        }   
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service started. Client id: {0}", connection.getId());
        
        // Retrieve the client side callback
        GameStatsSessionListener callback = getCallback(connection);
        
        // The newly connected client will be represented by this object on
        // the server side
        GameStatsSessionImpl session = new GameStatsSessionImpl(connection);
        
        connection.setAttribute(GAME_STATS_SERVICE, session);
        
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, session, GameStatsSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "GameStats service stopped: Client id: {0}", connection.getId());
    }
    
    private GameStatsSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        GameStatsSessionListener callback = rmiRegistry.getRemoteObject(GameStatsSessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for ChatSessionListener");
        }
        return callback;
    }

    /**
     * Adds a trap to the world.
     * @param trap type of trap
     * @param pos where the trap should be placed.
     */
    private void addTrapToWorld(TrapType trap, Vector3f pos){
        Trap trapGeom = createTrap(trap, pos);
        trapNode.attachChild(trapGeom);
        trapGeom.addToPhysicsSpace(bulletAppState);
    }
    
    public Trap createTrap(TrapType trap, Vector3f pos){
        switch(trap){
            case Freeze:
                return new FreezeTrap(pos);
            // TODO Create rest of traps
            default:
                return null;
        }
    }

    /**
     * TODO: When collision occurs:
     *      execute trap effect on player then delete the trap(From physics space too). 
     * @param event 
     */
    @Override
    public void collision(PhysicsCollisionEvent event) {
        LOGGER.info("Collison Occured!");
        Spatial a = event.getNodeA();
        Spatial b = event.getNodeB();
        String nameA = a.getName();
        String nameB = b.getName();
        if (playerCollideWithTrap(nameA, nameB)){
            // Collision with trap. NodeA is trap, B is player
        } else if (playerCollideWithTrap(nameB, nameA)){
            // Collision with trap. NodeB is trap, A is player
        } else if (isPlayer(nameA) && isPlayer(nameB)){
            LOGGER.info("Player collided with player");
            // Player collided with player
            // Dunno if supposed to check that here?    
        }
    }
    
    private boolean playerCollideWithTrap(String nameA, String nameB){
        if (nameA.equals("Trap")){
            if (isPlayer(nameB)) {
                LOGGER.info("Player Collided with trap!");
                
            }
        }
        return false;
    }
    
    private boolean isPlayer(String name){
        String[] playerName = name.split("#");
        if (playerName.length > 0) {
            if (playerName[0].equals("Player")) {
                return true;
            }
        }
        return false;
    }
    
    private void notifyPlayerTrapped(int playerID, TrapType trapType){
        
    }

    
    private class GameStatsSessionImpl implements GameStatsSession {
        
        private final HostedConnection connection;
        private GameStatsSessionListener callback;
        private PlayerStats stats;
        
        public GameStatsSessionImpl(HostedConnection connection){
            this.connection = connection;
        }

        @Override
        public void layTrap(TrapType trap, Vector3f pos) {
            Account account = connection.getAttribute(ConnectionAttribute.ACCOUNT);
            if (stats.decreaseTraps(trap) && account != null) { // Player has enough traps left
                addTrapToWorld(trap, pos);
            }
        }
    
    } 
    
}
