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
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.Human;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.login.client.ClientLoginService;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState {
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    private SimpleApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    
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
    
    
    @Override
    protected void initialize(Application app) {
        
        
        this.app = (SimpleApplication) app;
        
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
        
        Node playerNode = (Node) root.getChild("players");
        player = playerNode.getChild("player#" + ClientLoginService.getAccount().id);
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
    
    @Override
    public void update(float tpf){
        
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();

        camDir.y = 0;
        camLeft.y = 0;
        
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        walkingDirection.set(0,0,0);
        
        if(human.left) walkingDirection.addLocal(camLeft);
        if(human.right) walkingDirection.addLocal(camLeft.negate());
        if(human.forward) walkingDirection.addLocal(camDir);
        if(human.backward) walkingDirection.addLocal(camDir.negate());
        if(player != null){
            walkingDirection.multLocal(3f).multLocal(tpf);
            player.getControl(CharacterControl.class).setWalkDirection(walkingDirection);
        }
    }
    
}
