/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.Entity;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState {
    
    private SimpleApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    
    private Node root;
    private AssetManager asset;
    private InputManager input;
    private GameGUI game;
    
    
    private Spatial player;
    private int id;
    private boolean left = false, right = false, forward = false, backward = false;
    private final Vector3f walkingDirection = Vector3f.ZERO;
    private ChaseCamera chaseCamera;
    private Camera camera;
    
    public GameState(int entityId){
        this.id = entityId;
    }
    
    
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
        
        
        System.out.println("This screens id is : " + this.id + "\nChildren of root:\n" + root.getChildren());
        Node playerNode = (Node) root.getChild("players");
        System.out.println("Players : " + playerNode.getChildren().size());
        for(int i = 0; i < playerNode.getChildren().size(); i++){
            System.out.println(playerNode.getChild(i).getName());
            if(("player" + id).equals(playerNode.getChild(i).getName())){
                System.out.println("I'm playerNode id: " + "player"+id);
                chaseCamera = new ChaseCamera(camera, root.getChild("player" + id), input);
            }

        }
        
    }

    @Override
    protected void onDisable() {        
        app.stop();
    }
    
    @Override
    public void update(float tpf){
        /*
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();

        camDir.y = 0;
        camLeft.y = 0;
        
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        walkingDirection.set(0,0,0);
        
        if(left) walkingDirection.addLocal(camLeft);
        if(right) walkingDirection.addLocal(camLeft.negate());
        if(forward) walkingDirection.addLocal(camDir);
        if(backward) walkingDirection.addLocal(camDir.negate());
        
        if(root.getChild("players") != null){ 
            walkingDirection.multLocal(40f).multLocal(tpf);
            player.getController().setWalkDirection(walkingDirection);
        }
        */
    }
    
}
