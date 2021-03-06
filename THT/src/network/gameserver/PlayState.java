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
import control.CollisionController;
import control.HumanNode;
import control.MonkeyNode;
import control.MonsterNode;
import control.MyCharacterControl;
import control.WorldCreator;
import control.NPCController;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private CollisionController collisionController;
    private NPCController npcController;
    private BulletAppState bulletAppState;
    
    private ScheduledExecutorService movementSender;
    
    //Game Over : When all players are monsters, when all monkeys are caught.
    private int monkeys = 0;
    private int humans = 0;
    
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
        Node rooms = (Node) app.getRootNode().getChild("floor");
        
        if (playersNode == null || root == null || traps == null || rooms == null) {
            LOGGER.severe("root, trapNode, rooms or playersNode is null");
        }
        monkeys = 0;
        humans = 0;
        for(Spatial child : playersNode.getChildren()){
            //Count all the monkeys
            if(child instanceof MonkeyNode){
                monkeys++;
            }
            if(child instanceof HumanNode){
                humans++;
            }
        }
        LOGGER.log(Level.INFO, "Amount of humans " + humans + "\nAmount of monkeys " + monkeys);
        
        hostedMovementService.addSessions(this);        
        hostedGameStatsService.addSessions(this);
        movementSender = Executors.newScheduledThreadPool(1);
        movementSender.scheduleAtFixedRate(hostedMovementService.getMovementSender(playersNode, rooms), 20, 20, TimeUnit.MILLISECONDS);
        //movementSender.scheduleAtFixedRate(hostedMovementService.broadcastEverything(playersNode), 1, 1, TimeUnit.SECONDS);

        collisionController = new CollisionController(app.getStateManager().getState(PlayState.class), bulletAppState, root, hostedGameStatsService);

        npcController = new NPCController(root, app, hostedMovementService, bulletAppState);
    }

    @Override
    protected void onDisable() {
        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().removeAll(root);
        hostedMovementService.removeSessions(this);
        hostedGameStatsService.removeSessions(this);
        root.detachAllChildren();
        hostedMovementService.clear();
        movementSender.shutdownNow();
        collisionController.shutDown();
        collisionController = null;
        npcController.stopControlling();
        npcController = null;
    }

    @Override
    public void sendPlayerMovement(PlayerMovement playerMovement) {

        if (playersNode.getChild(playerMovement.id) == null) {
            LOGGER.severe("ID was wrong! ID: " + playerMovement.id);
            
        }else {
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    Spatial player = playersNode.getChild(playerMovement.id);
                    player.getControl(MyCharacterControl.class).setNextDirection(playerMovement.direction);
                    player.getControl(CharacterControl.class).setViewDirection(playerMovement.rotation);

                    hostedMovementService.playerUpdated(playerMovement.id);
                }
            });
        }
    }
    
    public void gameover(){
        PlayState ps = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                ps.setEnabled(false);
                app.getStateManager().getState(WaitingState.class).setEnabled(true);
            }
        });
    }
    
    public boolean allDead(){
        if(humans > 0) humans--;
        if(humans == 0){
            //GAME OVER
            LOGGER.log(Level.SEVERE, "Game over!");
            return true;
        }
        return false;
    }
    
    public void playerGotKilled(String victim, String killer){
        LOGGER.log(Level.INFO, victim + " got slaughtered by " + killer);
        
        if(playersNode.getChild(victim) == null || playersNode.getChild(killer) == null){
            LOGGER.severe("players does not exist");
        } else {

            //reset the player bullet
            app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playersNode.getChild(victim).getControl(GhostControl.class)); //reset bulletAppState
            app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playersNode.getChild(victim).getControl(CharacterControl.class)); //reset bulletAppState

            //remove old player
            playersNode.detachChildNamed(victim);
            
            //create new monster
            EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victim, bulletAppState);

            //ghost
            GhostControl ghost = new GhostControl(new BoxCollisionShape(new Vector3f(1f,2f,1f))); //test vector
            newMonster.addControl(ghost);            
            bulletAppState.getPhysicsSpace().add(ghost);
            
            //attach the new monster
            playersNode.attachChild(newMonster);
        } 
    }
    
    @Override
    public void update(float tpf){
        // Scale walking speed by tpf
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
                    
                    hostedGameStatsService.sendOutTraps(traps, geom.getName());
                }
            });

        }
    }

    public void deleteTrap(String name, String trapName){
        if(playersNode.getChild(name) != null){
            //Slow down player 
            EntityNode entity = (EntityNode) playersNode.getChild(name);
            entity.slowDown();
        }
        if(traps.getChild(trapName) != null){
            try{
                //remove from appState
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(traps.getChild(trapName).getControl(GhostControl.class)); //reset bulletAppState            
                //remove trap from root
                traps.detachChildNamed(trapName);
            } catch(NullPointerException e){
                LOGGER.log(Level.SEVERE, trapName+"'s ghostControl is null in bulletAppState! \n " + e.toString());
            }
                    
            if(traps.getChild(trapName) != null){
                LOGGER.log(Level.INFO, "trap was not removed");                
            }
        }
    }    
    
    public boolean allCaught(){
        if(monkeys > 0) monkeys--;
        if(monkeys == 0){
            return true;
        }
        return false;
    }

    public void monkeyGotCaught(String monkey){
        if(playersNode.getChild(monkey) == null){
            LOGGER.severe("monkey does not exist");
        } else {
            //reset the player bullet
            app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playersNode.getChild(monkey).getControl(GhostControl.class)); //reset bulletAppState
            app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playersNode.getChild(monkey).getControl(CharacterControl.class)); //reset bulletAppState

            //remove old player
            playersNode.detachChildNamed(monkey);            
        } 
    }

    @Override
    public void notifyJump(String player){
        if(playersNode.getChild(player) instanceof EntityNode){
            EntityNode entity = (EntityNode) playersNode.getChild(player);
            entity.jumped();            
            hostedGameStatsService.broadcastJump(player);            
        }
    }

    @Override
    public void notifySlash(String player) {
        if(playersNode.getChild(player) instanceof MonsterNode){
            hostedGameStatsService.broadcastSlash(player);        
        }
    }
}
