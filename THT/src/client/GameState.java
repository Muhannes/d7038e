/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;

import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import control.EntityNode;
import control.Human;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSessionListener;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.login.client.ClientLoginService;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState implements MovementSessionListener, GameStatsSessionListener{
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    private ClientApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    private ClientMovementService clientMovementService;
    private ClientGameStatsService clientGameStatsService;
    
    private MovementSessionListener movementListener;
    private GameStatsSessionListener gameStatsListener;
    
    private Node root;
    private Node traps;
    private Node playerNode;
    private AssetManager asset;
    private InputManager input;
    private GameGUI game;
    
    private EntityNode player;
    private ChaseCamera chaseCamera;
    private Camera camera;
    private Human human;
    private int id;
        
    @Override
    protected void initialize(Application app) {
        
        this.app = (ClientApplication) app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        game = new GameGUI(niftyDisplay);
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
    }

    @Override
    protected void cleanup(Application app) {
        if(root != null){
            root.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {     
        movementListener = this;
        gameStatsListener = this;
        
        this.root = app.getRootNode();
        this.traps = (Node) app.getRootNode().getChild("traps");            
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        
        /* Listeners */
        this.clientMovementService = app.getClientMovementService();        
        this.clientMovementService.addListener(movementListener);
        
        this.clientGameStatsService = app.getClientGameStatsService();
        this.clientGameStatsService.addGameStatsSessionListener(gameStatsListener);
        
        playerNode = (Node) root.getChild("players");
        player = (EntityNode) playerNode.getChild(""+ClientLoginService.getAccount().id);

        if(player == null){
            LOGGER.log(Level.SEVERE, "player is null");
        }
        if(camera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        if(input == null){
            LOGGER.log(Level.SEVERE, "inputmanager is null");
        }
        
        // set forward camera node that follows the character
        /*CameraNode camNode = new CameraNode("CamNode", camera);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 1, -5));
        camNode.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
        player.attachChild(camNode);
        */
        chaseCamera = new ChaseCamera(camera, player, input);
        chaseCamera.setMaxDistance(12);
        
        if(chaseCamera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        human = new Human(player, app, clientMovementService, clientGameStatsService);
        human.initKeys(input);               

    }

    @Override
    protected void onDisable() {        
        app.stop();
    }
    
    @Override
    public void update(float tpf){
        // Scale walking speed by tpf
        for (Spatial entity : ((Node)app.getRootNode().getChild("players")).getChildren()) {
            ((EntityNode) entity).scaleWalkDirection(tpf);
        }
        
    }
    
    @Override
    public void newMessage(List<PlayerMovement> playerMovements) {        
        app.enqueue(() -> {
            convergePlayers(playerMovements);
        });
    }
    
    /**
     * converges all players that have changed their movementStatus.
     * currently only snaps. Should be linear convergence for characters that moves "short distances".
     * @param playerMovements 
     */
    private void convergePlayers(List<PlayerMovement> playerMovements){
     
        Node players = (Node) root.getChild("players");
        for (PlayerMovement playerMovement : playerMovements) {
            if (playerMovement.id.equals(player.getName())) { // This player
                LOGGER.log(Level.INFO, "Converging self: {0}", playerMovement.id);
                player.convergeSnap(playerMovement.location, player.getWalkDirection(), player.getLocalRotation());
            } else { // Other entity, converge
                LOGGER.log(Level.INFO, "Converging player: {0}", playerMovement.id);
                EntityNode entity = (EntityNode) players.getChild(playerMovement.id);
                entity.convergeSnap(playerMovement.location, playerMovement.direction, playerMovement.rotation);
            }
        }
        
    }
    
    @Override
    public void notifyPlayersKilled(List<String> victims, List<String> killers) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyPlayersEscaped(List<String> names) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapsPlaced(List<String> trapNames, List<Vector3f> newTraps) {
        app.enqueue(() -> {
            updateTreeWithNewTraps(trapNames, newTraps);
        });    
    }

    public void updateTreeWithNewTraps(List<String> trapNames, List<Vector3f> newTraps){
                        
        for(int i = 0; i < trapNames.size(); i++){
            if(traps.getChild(trapNames.get(i)) != null){
                LOGGER.log(Level.SEVERE, "trap already exist, dont add");
            } else {
                //Create a trap at the location with the name given.
                Box box = new Box(0.1f,0.1f,0.1f);
                Geometry geom = new Geometry(trapNames.get(i), box);
                Material material = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
                material.setColor("Color", ColorRGBA.Red);
                geom.setMaterial(material);

                Vector3f position = newTraps.get(i);
                position.y = 0.1f;
                geom.setLocalTranslation(position);        

                //Create node for each Trap (Only server needs to control check ghosts)
                Node node = new Node(trapNames.get(i));
                node.attachChild(geom);
                traps.attachChild(node);
            }
        }
    }
    
    @Override
    public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
/*        for(int i = 0; i < names.size(); i++){
            //The players that triggered the corresponding trap is slowed.
            traps.detachChildNamed(trapNames.get(i));
            playerNode = (Node) root.getChild("players");
            EntityNode triggerer = (EntityNode) playerNode.getChild(names.get(i));
            triggerer.setWalkDirection(triggerer.getWalkDirection().mult(0.5f));
        }*/
    }

}
