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
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
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
    
    private Spatial player;
    
    private CharacterControl playerControl;
    
    private CapsuleCollisionShape playerShape;
    
    private InputManager input;
    
    private FlyByCamera flyCam;
    
    private ChaseCamera chaseCamera;
    
    private Camera camera;
    
    private final Vector3f walkingDirection = Vector3f.ZERO;
    
    private boolean left = false, right = false, forward = false, backward = false;
    
    
    public SetupState(ClientGameSetupService cgss, int id){
        this.cgss = cgss;
        this.globalId = id;
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;    
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: cleanup for setup state
        root.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        this.root = app.getRootNode();
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        
        flyCam = app.getFlyByCamera();
        
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
            flyCam.setMoveSpeed(20);
            flyCam.setEnabled(false);
            
            
            //Notify ready
            System.out.println("In notifyEvent, load up everything on screen.");
            
        } else if (T == StartGameEvent.class){
            this.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);            
        }
    }
    
    
    public void buildStaticWorld(){
        
        //Init bulletAppState maybe somewhere else
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);
        
        app.getStateManager().attach(bulletAppState);
        
        root.attachChild(worldRoot);
        Spatial scene = asset.loadModel("Scenes/world.j3o");
        worldRoot.attachChild(scene);
        
        // Loading floor child to world node
        Spatial floor = worldRoot.getChild("floor");
        bulletAppState.getPhysicsSpace().add(floor.getControl(RigidBodyControl.class));
        
        //Loading player to world node
        player = worldRoot.getChild("player");
        BoundingBox boundingBox = (BoundingBox) player.getWorldBound();
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        
        playerShape = new CapsuleCollisionShape(radius, height);
                
        playerControl = new CharacterControl(playerShape, 1.0f);
        player.addControl(playerControl);

        bulletAppState.getPhysicsSpace().add(playerControl);
        
        
        input.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        input.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        input.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        input.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        input.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        input.addListener(actionListener, "Left", "Right", "Forward", "Backward", "Jump");
        
        chaseCamera = new ChaseCamera(camera, player, input);
    }
    
    private final ActionListener actionListener = new ActionListener(){
        @Override
        public void onAction(String name, boolean keyPressed, float tpf){
            if(name.equals("Left")){
                left = keyPressed;
            }
            else if(name.equals("Right")){
                right = keyPressed;
            }
            else if(name.equals("Forward")){
                forward = keyPressed;
            }
            else if(name.equals("Backward")){
                backward = keyPressed;
            }
            else if (name.equals("Jump")){
                playerControl.jump();
            } else {
                
            }
        }
    };
    
    @Override
    public void update(float tpf){
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
        
        if(player != null){
            walkingDirection.multLocal(10f).multLocal(tpf);
            playerControl.setWalkDirection(walkingDirection);
        }
        
    }
    
}
