/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.Human;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.login.client.ClientLoginService;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState implements MovementSessionListener{
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    private ClientApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    private ClientMovementService clientMovementService;
    
    private Node root;
    private AssetManager asset;
    private InputManager input;
    private GameGUI game;
    
    
    private Spatial player;
    private Spatial playerSpatial;
    private boolean left = false, right = false, forward = false, backward = false;
    private Vector3f walkingDirection = Vector3f.ZERO;
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
        this.root = app.getRootNode();
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        this.clientMovementService = app.getClientMovementService();
        
        Node playerNode = (Node) root.getChild("players");
        player = playerNode.getChild(""+ClientLoginService.getAccount().id);
        if(player == null){
            LOGGER.log(Level.SEVERE, "player is null");
        }
        if(camera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        if(input == null){
            LOGGER.log(Level.SEVERE, "inputmanager is null");
        }
        
        chaseCamera = new ChaseCamera(camera, player, input);
        if(chaseCamera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        human = new Human(player, app);
        human.initKeys(input);               

    }

    @Override
    protected void onDisable() {        
        app.stop();
    }
    
    private void sendToServer(Vector3f location, Vector3f direction, Vector3f rotation){   
        PlayerMovement pm = new PlayerMovement(player.getName(), location, direction, rotation);
        clientMovementService.sendMessage(pm);
    }
    
    @Override
    public void update(float tpf){
        
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();

        camDir.y = 0;
        camLeft.y = 0;
        
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        walkingDirection.set(0,0,0);
        
        if(human.left){
            walkingDirection.addLocal(camLeft);
            sendToServer(player.getLocalTranslation(), 
                    player.getControl(CharacterControl.class).getWalkDirection(), 
                    player.getControl(CharacterControl.class).getViewDirection());
        }
        if(human.right){
            walkingDirection.addLocal(camLeft.negate());
            sendToServer(player.getLocalTranslation(), 
                    player.getControl(CharacterControl.class).getWalkDirection(), 
                    player.getControl(CharacterControl.class).getViewDirection());
        }
        if(human.forward){
            walkingDirection.addLocal(camDir);
            sendToServer(player.getLocalTranslation(), 
                    player.getControl(CharacterControl.class).getWalkDirection(), 
                    player.getControl(CharacterControl.class).getViewDirection());
        }
        if(human.backward){
            walkingDirection.addLocal(camDir.negate());
            sendToServer(player.getLocalTranslation(), 
                    player.getControl(CharacterControl.class).getWalkDirection(), 
                    player.getControl(CharacterControl.class).getViewDirection());
        }
        if(player != null){
            walkingDirection.multLocal(human.movementSpeed).multLocal(tpf);
            player.getControl(CharacterControl.class).setWalkDirection(walkingDirection);
        }
    }

    public void youAreTrapped(){
    }
    
    @Override
    public void newMessage(List<PlayerMovement> playerMovements) {
        app.enqueue(() -> {
            convergePlayers(playerMovements);
        });
    }
   
    
    private void convergePlayers(List<PlayerMovement> playerMovements){
     
        Node players = (Node) root.getChild("players");
        for(PlayerMovement newPlayerInfo : playerMovements){
            Spatial playerNode = (Spatial) players.getChild(newPlayerInfo.id);
            Vector3f newLocation = newPlayerInfo.location;
            Vector3f newDirection = newPlayerInfo.direction.subtract(playerNode.getControl(CharacterControl.class).getWalkDirection());
            Vector3f newRotation = newPlayerInfo.rotation;
            //Quaternion newRotation = newPlayerInfo.rotation;
            
            playerNode.setLocalTranslation(newLocation);
            //playerNode.setLocalRotation(newRotation);
            playerNode.getControl(CharacterControl.class).setViewDirection(newLocation);
            playerNode.getControl(CharacterControl.class).setWalkDirection(newDirection);

            //TODO: If the player is too far away from the action direction, do snap.
        }
    }
    
}
