/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.Entity;
import api.models.EntityType;
import api.models.Player;
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
import control.Human;
import de.lessvoid.nifty.Nifty;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
public class SetupState extends BaseAppState implements 
        //EventListener, 
        GameSetupSessionListener{

    private SimpleApplication app;
    
    private ClientGameSetupService cgss;
    
    private int globalId;
    
    private List<Player> players;
    
    private PlayerInitEvent playerEvent;
    
    private Entity playerEntity;
    
    private Node root;
    
    private Node worldRoot = new Node("World"); //Bind the scene to this node.
    
    private AssetManager asset;
    
    private Spatial avatar;
    
    private CapsuleCollisionShape playerShape;
    
    private BulletAppState bulletAppState;
    
    private InputManager input;
    
    private FlyByCamera flyCam;
    
    private ChaseCamera chaseCamera;
    
    private Camera camera;
    
    private final Vector3f walkingDirection = Vector3f.ZERO;
    
    private boolean left = false, right = false, forward = false, backward = false;
    
    private Human human = new Human();
    
    
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
        cgss.addGameSetupSessionListener(this);
        
        //EventBus.subscribe(this);
        cgss.join(globalId);        
        System.out.println("Setup is enabled");
            
        //Bullet physics for players, walls, objects
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);        
        buildStaticWorld(); //When the world is built, send it to gameState
        
        //SEND TO SERVER THAT GAME CAN START
        
    }

    @Override
    protected void onDisable() {
        // TODO: destroy stuff not needed anymore( if there is anything?)
        cgss.removeGameSetupSessionListener(this);
    }

/*    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if(T == PlayerInitEvent.class){
            //INIT WORLD
            System.out.println("NEW PLAYER INIT EVENT!\nglobalID : " + globalId);
            playerEvent = (PlayerInitEvent) event;
            players = playerEvent.players;
            
            
            flyCam.setEnabled(true);
            
            //Notify ready
            System.out.println("In notifyEvent, load up everything on screen.");
            cgss.ready();
            
        } else if (T == StartGameEvent.class){
            System.out.println("START GAME EVENT");
            SetupState ss = this;
            app.enqueue(() -> {
                ss.setEnabled(false);
                app.getStateManager().getState(GameState.class).setEnabled(true);
            });         
        }
    }
*/    
    
    public void buildStaticWorld(){        
        
        //Init bulletAppState maybe somewhere else
        app.getStateManager().attach(bulletAppState);
        
        buildStructures();
        createPlayers();
        
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
        System.out.println("How many players do we have ? answer : " + players.size());
        for(Player player : players){
            Entity entity = new Entity(asset, player.getPosition(), player.getID());
            root.attachChild(entity.getGeometry()); 
            bulletAppState.getPhysicsSpace().add(entity.getController());   

            //RECEIVED MY OWN ENTITY FROM SERVER 
            
/*            if(this.globalId == entity.getId()){
                playerEntity = entity;
                playerEntity.setColor(ColorRGBA.Red);
                
                avatar = root.getChild("player" + entity.getId());  
                System.out.println("Avatar : " + avatar.getName());
                avatar.addControl(entity.getController());    
                chaseCamera = new ChaseCamera(camera, avatar, input);
                
                human.setEntity(playerEntity);
                human.initKeys(input);
            } */
        } 
        
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
        
        if(avatar != null){ 
            walkingDirection.multLocal(40f).multLocal(tpf);
            playerEntity.getController().setWalkDirection(walkingDirection);
        }
    }

    @Override
    public void initPlayer(List<Player> players) {
        System.out.println("NEW PLAYER INIT EVENT!\nglobalID : " + globalId);
        players = playerEvent.players;

        flyCam.setEnabled(true);

        //Notify ready
        System.out.println("In notifyEvent, load up everything on screen.");
        cgss.ready();
    }

    @Override
    public void startGame() {
        System.out.println("START GAME EVENT");
        SetupState ss = this;
        app.enqueue(() -> {
            ss.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);
        });         
    }
    
}
