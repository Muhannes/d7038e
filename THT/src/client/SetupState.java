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
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import network.services.gamesetup.ClientGameSetupService;
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
    
    private BulletAppState bulletAppState;
    
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
        
        /* Not required here, only setup!
        
        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
                
        // Create a new NiftyGUI object 
        Nifty nifty = niftyDisplay.getNifty();

        // Read your XML and initialize your custom ScreenController 
        nifty.fromXml("Interface/gameState.xml", "frame"); //Should be 2 screens, human and monster. 
        // attach the Nifty display to the gui view port as a processor
        app.getViewPort().addProcessor(niftyDisplay);
        */
                
        System.out.println("Setup is enabled");
            
        //Bullet physics for players, walls, objects
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);        
        buildStaticWorld(); //When the world is built, send it to gameState
        
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if(T == PlayerInitEvent.class){
            //INIT WORLD
            
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
        app.getStateManager().attach(bulletAppState);
        
        buildStructures(); //Only this should remain in setup. Move rest to gameState.
        createPlayers();
        
        //Key boundings for players
        input.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        input.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        input.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        input.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        input.addMapping("Trap", new KeyTrigger(KeyInput.KEY_F));        
        input.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        input.addListener(actionListener, "Left", "Right", "Forward", "Backward", "Jump", "Trap");
        chaseCamera = new ChaseCamera(camera, player, input);
    }
    
    public void buildStructures(){
        
        root.attachChild(worldRoot);
        Spatial scene = asset.loadModel("Scenes/world.j3o");
        worldRoot.attachChild(scene);
        
        Material ground_mat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        ground_mat.setColor("Color", ColorRGBA.Gray);     
        
        Material walls_mat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        walls_mat.setColor("Color", ColorRGBA.DarkGray);     

        Material innerWalls_mat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        innerWalls_mat.setColor("Color", ColorRGBA.Brown);     
        
        
        Spatial floor1 = worldRoot.getChild("floor1");
        floor1.setMaterial(ground_mat);
        bulletAppState.getPhysicsSpace().add(floor1.getControl(RigidBodyControl.class));
        
        Spatial floor2 = worldRoot.getChild("floor2");
        floor2.setMaterial(ground_mat);
        bulletAppState.getPhysicsSpace().add(floor2.getControl(RigidBodyControl.class));       
        
        Spatial floor3 = worldRoot.getChild("floor3");
        floor3.setMaterial(ground_mat);
        bulletAppState.getPhysicsSpace().add(floor3.getControl(RigidBodyControl.class));
        
        Spatial wall1 = worldRoot.getChild("wall1");
        wall1.setMaterial(walls_mat);
        bulletAppState.getPhysicsSpace().add(wall1.getControl(RigidBodyControl.class));
        
        Spatial wall2 = worldRoot.getChild("wall2");
        wall2.setMaterial(walls_mat);
        bulletAppState.getPhysicsSpace().add(wall2.getControl(RigidBodyControl.class));
        
        Spatial wall3 = worldRoot.getChild("wall3");
        wall3.setMaterial(walls_mat);
        bulletAppState.getPhysicsSpace().add(wall3.getControl(RigidBodyControl.class));
        
        Spatial wall4 = worldRoot.getChild("wall4");
        wall4.setMaterial(walls_mat);
        bulletAppState.getPhysicsSpace().add(wall4.getControl(RigidBodyControl.class));
        
        //Inner walls
        
        Spatial innerWall1 = worldRoot.getChild("innerWall1");
        innerWall1.setMaterial(innerWalls_mat);
        CollisionShape tmp1 = innerWall1.getControl(RigidBodyControl.class).getCollisionShape();
        tmp1.setScale(new Vector3f(25,25,1));
        innerWall1.getControl(RigidBodyControl.class).setCollisionShape(tmp1);
        bulletAppState.getPhysicsSpace().add(innerWall1.getControl(RigidBodyControl.class));
        
        Spatial innerWall2 = worldRoot.getChild("innerWall2");
        innerWall2.setMaterial(innerWalls_mat);
        CollisionShape tmp2 = innerWall2.getControl(RigidBodyControl.class).getCollisionShape();
        tmp2.setScale(new Vector3f(25,25,1));
        innerWall2.getControl(RigidBodyControl.class).setCollisionShape(tmp2);
        bulletAppState.getPhysicsSpace().add(innerWall2.getControl(RigidBodyControl.class));
        
        Spatial innerWall3 = worldRoot.getChild("innerWall3");
        innerWall3.setMaterial(innerWalls_mat);
        CollisionShape tmp3 = innerWall3.getControl(RigidBodyControl.class).getCollisionShape();
        tmp3.setScale(new Vector3f(25,25,1));
        innerWall3.getControl(RigidBodyControl.class).setCollisionShape(tmp3);
        bulletAppState.getPhysicsSpace().add(innerWall3.getControl(RigidBodyControl.class));
        
    }
    
    public void createPlayers(){
        
        //Create monster
        Texture demonSkin = asset.loadTexture("Models/demon/demon_tex.png");
        Material demonMat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        demonMat.setTexture("ColorMap", demonSkin);
        Spatial demon = worldRoot.getChild("demon");     
        demon.setMaterial(demonMat);
        bulletAppState.getPhysicsSpace().add(demon.getControl(RigidBodyControl.class));

        createHumans();           
    }
    
    public void createHumans(){
        //Create a player blob
        player = worldRoot.getChild("player1");
        BoundingBox boundingBox = (BoundingBox) player.getWorldBound();
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();        
        playerShape = new CapsuleCollisionShape(radius, height);                
        playerControl = new CharacterControl(playerShape, 1.0f);
        player.addControl(playerControl);
        bulletAppState.getPhysicsSpace().add(playerControl);        
        
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
            } else if (name.equals("Trap") && keyPressed){
                Vector3f location = player.getLocalTranslation();
                //Send message about the new trap at a location of the player.
                putTrap(location); //Should be removed when server does the same thing.
            }else {
                
            }
        }
    };
    
    public void putTrap(Vector3f trapLocation){
        Box box = new Box(1,1,1);
        Geometry geom = new Geometry("Trap", box);
        geom.setLocalTranslation(trapLocation);
        Material box_mat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        box_mat.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(box_mat);        
        worldRoot.attachChild(geom);
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
        
        if(left) walkingDirection.addLocal(camLeft);
        if(right) walkingDirection.addLocal(camLeft.negate());
        if(forward) walkingDirection.addLocal(camDir);
        if(backward) walkingDirection.addLocal(camDir.negate());
        
        if(player != null){
            walkingDirection.multLocal(40f).multLocal(tpf);
            playerControl.setWalkDirection(walkingDirection);
        }
    }
    
}
