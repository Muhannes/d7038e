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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.Human;
import java.util.List;
import java.util.logging.Level;
import network.services.gamesetup.ClientGameSetupService;
import network.services.gamesetup.GameSetupSessionListener;
import network.services.gamesetup.PlayerInitEvent;
import network.services.login.HostedLoginService;

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
    
    private BulletAppState bulletAppState;
    
    private InputManager input;
    
    private FlyByCamera flyCam;
    
    private ChaseCamera chaseCamera;
    
    private Camera camera;
    
    private Human human = new Human();
    
    
    public SetupState(int id){
        this.globalId = id;
        
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;    
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: cleanup for setup state
        if(root != null){
            root.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {
        this.cgss = ((ClientApplication)app).getGameSetupService();
        this.root = app.getRootNode();
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        
        flyCam = app.getFlyByCamera();
        cgss.addGameSetupSessionListener(this);
        
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        try {
            Thread.sleep(350);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(HostedLoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        cgss.join(globalId);
            
        //Bullet physics for players, walls, objects
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);        
        buildStaticWorld(); //When the world is built, send it to gameState
        
    }

    @Override
    protected void onDisable() {
        // TODO: destroy stuff not needed anymore( if there is anything?)
        cgss.removeGameSetupSessionListener(this);
    }
    
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

        Node floors = new Node();
        floors = (Node) worldRoot.getChild("floor");
        
        for(int i = 0; i < floors.getChildren().size(); i++){
            RigidBodyControl rigidBodyControl = new RigidBodyControl();
            floors.getChild(i).setMaterial(ground_mat);
            floors.getChild(i).addControl(rigidBodyControl);            
            bulletAppState.getPhysicsSpace().add(rigidBodyControl);
        }

        Node walls = new Node();
        walls = (Node) worldRoot.getChild("walls");        
        
        for(int i = 0; i < walls.getChildren().size(); i++){
            RigidBodyControl rigidBodyControl = new RigidBodyControl();
            walls.getChild(i).setMaterial(walls_mat);
            walls.getChild(i).addControl(rigidBodyControl);
            bulletAppState.getPhysicsSpace().add(rigidBodyControl);
        }

        Node innerWalls = new Node();
        innerWalls = (Node) worldRoot.getChild("innerWalls");

        for(int i = 0; i < innerWalls.getChildren().size(); i++){
            RigidBodyControl rigidBodyControl = new RigidBodyControl();
            innerWalls.getChild(i).setMaterial(innerWalls_mat);
            innerWalls.getChild(i).addControl(rigidBodyControl);            
            bulletAppState.getPhysicsSpace().add(rigidBodyControl);
        }
    }
    
    public void createPlayers(){
        System.out.println("How many players do we have ? answer : " + players.size());
        for(Player player : players){
            Entity entity = new Entity(asset, player.getPosition(), player.getID());
            root.attachChild(entity.getGeometry()); 
            bulletAppState.getPhysicsSpace().add(entity.getController());   
        }         
    }
     
    @Override
    public void update(float tpf){

    }

    @Override
    public void initPlayer(List<Player> listOfPlayers) {
        players = listOfPlayers;

        flyCam.setEnabled(true);

        //Notify ready
        cgss.ready();
    }

    @Override
    public void startGame() {
        System.out.println("Start gamestate from setupstate!");
        SetupState ss = this;
        app.enqueue(() -> {
            ss.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);
        });         
    }
    
}
