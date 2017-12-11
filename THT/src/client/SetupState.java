/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.Player;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import de.lessvoid.nifty.Nifty;
import network.services.gamesetup.ClientGameSetupService;
import network.services.gamesetup.GameSetupSessionListener;
import network.services.gamesetup.PlayerInitEvent;
import network.services.gamesetup.StartGameEvent;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author ted
 */
public class SetupState extends BaseAppState implements EventListener{

    private SimpleApplication app;
    
    private ClientGameSetupService cgss;
    
    private int globalId;
    
    private Node root;
    
    private Node worldRoot = new Node("World"); //Bind the scene to this node.
    
    private AssetManager asset;
        
    public SetupState(ClientGameSetupService cgss, int id){
        this.cgss = cgss;
        this.globalId = id;
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;    
        this.asset = app.getAssetManager();
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: cleanup for setup state
        root.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        this.root = app.getRootNode();
        EventBus.subscribe(this);
        cgss.join(globalId);
        
        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
                
        /** Create a new NiftyGUI object */
        Nifty nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gameState.xml", "frame"); //Should be 2 screens, human and monster. 
        System.out.println("Setup is enabled");
        // attach the Nifty display to the gui view port as a processor
        app.getViewPort().addProcessor(niftyDisplay);
            
        System.out.println("Building World!");
        buildStaticWorld();
        System.out.println("Built World!");
        
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if(T == PlayerInitEvent.class){
            //INIT WORLD
            app.getRootNode();
            FlyByCamera flyCam = app.getFlyByCamera();
            flyCam.setMoveSpeed(50);
            flyCam.setEnabled(true);
            
            
            //Notify ready
            System.out.println("In notifyEvent, load up everything on screen.");
            
        } else if (T == StartGameEvent.class){
            this.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);            
        }
    }
    
    
    public void buildStaticWorld(){
        
        root.attachChild(worldRoot);
        Spatial scene = asset.loadModel("Scenes/world.j3o");
        worldRoot.attachChild(scene);
    }
    
}
