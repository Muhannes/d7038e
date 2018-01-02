/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import control.EntityNode;
import control.TrapController;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSession;
import network.service.gamestats.server.HostedGameStatsService;
import network.service.movement.MovementSession;
import network.service.movement.PlayerMovement;
import network.service.movement.server.HostedMovementService;

/**
 *
 * @author ted
 */
public class PlayState extends BaseAppState implements MovementSession, GameStatsSession{

    private static final Logger LOGGER = Logger.getLogger(PlayState.class.getName());
    private GameServer app;
    private Node playersNode;
    private Node root;
    private Node traps;
    private HostedMovementService hostedMovementService;
    private HostedGameStatsService hostedGameStatsService;
    private TrapController trapController;
    private BulletAppState bulletAppState;
    
    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;
        this.bulletAppState = app.getStateManager().getState(BulletAppState.class);
        
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        LOGGER.info("Playstate enabled!");
        hostedMovementService = app.getHostedMovementService();
        hostedGameStatsService = app.getHostedGameStatsService();
        
        root = (Node) app.getRootNode();
        playersNode = (Node) app.getRootNode().getChild("playersNode");
        traps = (Node) app.getRootNode().getChild("traps");
        
        if (playersNode == null || root == null || traps == null) {
            LOGGER.severe("root, trapNode or playersNode is null");
        }
        
        hostedMovementService.addSessions(this);        
        hostedGameStatsService.addSessions(this);
        hostedMovementService.sendOutMovements(playersNode);
        hostedGameStatsService.sendOutTraps(traps);        

        trapController = new TrapController(app.getStateManager().getState(PlayState.class), bulletAppState, root, hostedGameStatsService);

    }

    @Override
    protected void onDisable() {
        hostedMovementService.removeSessions(this);
        hostedGameStatsService.removeSessions(this);
    }

    @Override
    public void sendPlayerMovement(PlayerMovement playerMovement) {

        if (playersNode.getChild(playerMovement.id) == null) {
            LOGGER.severe("ID was wrong!");
        }else {
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    playersNode.getChild(playerMovement.id).setLocalTranslation(playerMovement.location);
                    playersNode.getChild(playerMovement.id).getControl(CharacterControl.class).setWalkDirection(playerMovement.direction);
                    playersNode.getChild(playerMovement.id).setLocalRotation(playerMovement.rotation);

                    hostedMovementService.playerUpdated(playerMovement.id);
                }
            });
        }
    }
    
    @Override
    public void update(float tpf){
        for (Spatial entity : playersNode.getChildren()) {
            ((EntityNode) entity).scaleWalkDirection(tpf);
        }
    }

    @Override
    public void notifyPlayerKilled(String victim, String killer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyPlayerEscaped(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapPlaced(String trapName, Vector3f newTrap) {
        if (traps.getChild(trapName) != null) {
            LOGGER.severe("ID already exist! " + trapName);
        }else {
            app.enqueue(new Runnable() {
                @Override
                public void run() {
 
                    Box box = new Box(0.1f,0.1f,0.1f);
                    Geometry geom = new Geometry(trapName, box);
                    Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    material.setColor("Color", ColorRGBA.Red);
                    geom.setMaterial(material);
            
                    //Create node for each Trap
                    Node node = new Node(trapName);
                    node.attachChild(geom);

                    GhostControl ghost = new GhostControl(new BoxCollisionShape(new Vector3f(0.1f,0.1f,0.1f)));
                    node.addControl(ghost);
                            
                    Vector3f position = newTrap;                
                    position.y = 0.1f; 
                    node.setLocalTranslation(position);
                    ghost.setPhysicsLocation(position);
                    node.getControl(GhostControl.class).setSpatial(geom);
                    
                    bulletAppState.getPhysicsSpace().add(ghost);
                    traps.attachChild(node);
                    hostedGameStatsService.trapUpdated(geom.getName());
                }
            });
        }
    }

    @Override
    public void notifyTrapTriggered(String name, String trapName) { //should not be needed
 /*       System.out.println("nofityTrapTriggered in PlayState" + name + " - " + trapName);
        traps.detachChildNamed(trapName);
        EntityNode entity = (EntityNode) playersNode.getChild(name);
        //Slow down entity.
        entity.slowDown();*/
    }

    @Override
    public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
    app.enqueue(() -> {
            updateTreeWithDeletedTraps(names, trapNames);
        });    
    }
    
    public void updateTreeWithDeletedTraps(List<String> names, List<String> trapNames){
        LOGGER.log(Level.INFO, names + " \n " + trapNames);
        List<String> updatedNames = new ArrayList<>();
        List<String> updatedTrapNames = new ArrayList<>();
        
        for(int i = 0; i < trapNames.size(); i++){
            if(!updatedTrapNames.contains(trapNames.get(i))){
                traps.detachChildNamed(trapNames.get(i));
                updatedTrapNames.add(trapNames.get(i));
            }
        }

        for(int j = 0; j < names.size(); j++){
            if(!updatedNames.contains(names.get(j))){
                EntityNode entity = (EntityNode) playersNode.getChild(names.get(j));
                entity.slowDown();
                updatedNames.add(names.get(j));
            }
        }    
    }    

    public void deleteTrap(String name, String trapName){
        LOGGER.log(Level.INFO, name + " \n " + trapName);
        if(playersNode.getChild(name) != null){
            //Slow down player 
            EntityNode entity = (EntityNode) playersNode.getChild(name);
            entity.slowDown();
            LOGGER.log(Level.INFO, entity.getName() + " is slowed");
        }
        if(traps.getChild(trapName) != null){
            //remove trap from root
            traps.detachChildNamed(trapName);
            if(traps.getChild(trapName) != null){
                LOGGER.log(Level.INFO, "trap was not removed");                
            }
        }
    }    
}
