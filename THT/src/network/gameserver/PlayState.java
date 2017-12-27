/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import control.Entity;
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
    private Node trapNode;
    private HostedMovementService hostedMovementService;
    private HostedGameStatsService hostedGameStatsService;

    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        LOGGER.info("Playstate enabled!");
        hostedMovementService = app.getHostedMovementService();
        hostedGameStatsService = app.getHostedGameStatsService();
        
        playersNode = (Node) app.getRootNode().getChild("playersNode");
        trapNode = (Node) app.getRootNode().getChild("traps");
        root = (Node) app.getRootNode();
        
        if (playersNode == null || root == null || trapNode == null) {
            LOGGER.severe("root, trapNode or playersNode is null");
        }
        
        hostedMovementService.addSessions(this);        
        hostedGameStatsService.addSessions(this);
        hostedMovementService.sendOutMovements(playersNode);
        hostedGameStatsService.sendOutTraps(trapNode, playersNode);        
    }

    @Override
    protected void onDisable() {
        hostedMovementService.removeSessions(this);
        hostedGameStatsService.removeSessions(this);
    }

    @Override
    public void sendMessage(PlayerMovement playerMovement) {
        
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
            ((Entity) entity).scaleWalkDirection(tpf);
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
        if (root.getChild(trapName) == null) {
            LOGGER.severe("ID was wrong!");
        }else {
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    //Create a new trap
                    Box box = new Box(0.1f,0.1f,0.1f);
                    Geometry geom = new Geometry(trapName, box);
                    Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    material.setColor("Color", ColorRGBA.Red);
                    geom.setMaterial(material);
                    Vector3f position = newTrap;
                    position.y = 0.1f;
                    geom.setLocalTranslation(position);      
                    trapNode.attachChild(geom);
                    hostedGameStatsService.sendOutTraps(trapNode, playersNode);
                }
            });
        }
    }

    @Override
    public void notifyTrapTriggered(String name, String trapName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
