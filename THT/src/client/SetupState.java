/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.Player;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
<<<<<<< HEAD
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.light.AmbientLight;
=======
>>>>>>> 632be89c7f3db7c55e7c9704d3a73345b53bb62c
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.LightNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.EntityNode;
import control.WorldCreator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamesetup.client.ClientGameSetupService;
import network.service.gamesetup.GameSetupSessionListener;
import network.service.login.Account;
import network.service.login.client.ClientLoginService;

/**
 * This state is used to set up the game
 * @author ted
 */
public class SetupState extends BaseAppState implements 
        GameSetupSessionListener{
    
    private static final Logger LOGGER = Logger.getLogger(SetupState.class.getName());

    private ClientApplication app;
    
    private ClientGameSetupService gameSetupService;
    
    private Node world;
    
    private AssetManager asset;
    
    private BulletAppState bulletAppState;
    
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;  
        bulletAppState = app.getStateManager().getState(BulletAppState.class);
        
        
    }

    @Override
    protected void cleanup(Application app) {
        if(world != null){ // NOTE: Should this be done here or at some higher level?
            world.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {
        world = new Node("world");
        this.app.getRootNode().attachChild(world);
        gameSetupService = app.getGameSetupService();
        asset = app.getAssetManager();
        
        // Will notify us when when eveyone is ready
        gameSetupService.addGameSetupSessionListener(this);
        
        Account acc = ClientLoginService.getAccount();
        LOGGER.info("Sending join to server...");
        gameSetupService.join(acc.id, acc.key, acc.name);
        
        loadStaticGeometry();
    }

    @Override
    protected void onDisable() {
        gameSetupService.removeGameSetupSessionListener(this);
    }

    @Override
    public void initPlayer(List<Player> listOfPlayers) {
        
        app.enqueue(() -> {
            createPlayers(listOfPlayers);
        });
    }

    @Override
    public void startGame() {
        app.enqueue(() -> {
            this.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);
        });         
    }
    
    private void loadStaticGeometry(){   
        Spatial creepyhouse = asset.loadModel("Scenes/creepyhouse.j3o");
        creepyhouse.setName("creepyhouse");
        world.attachChild(creepyhouse);
        
        
        LightNode lightNode = new LightNode();
        // This light is for all traps so they become visible
        
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(0.4f, 0.03f, 0.05f, 1.0f));
        dl.setDirection(new Vector3f(0f, -1.f, 0f));
        
        lightNode.addLight(dl);
        
        Node traps = new Node("traps");
        // Node that holds the traps
        
        lightNode.attachChild(traps);
        // Important that traps are subnodes to the lightnode
        
        app.getRootNode().attachChild(lightNode);
        
        if (bulletAppState != null) {
            WorldCreator.addPhysicsToMap(bulletAppState, creepyhouse);
        } else {
            LOGGER.severe("bulletAppState Was null when initializing world");
        }
    }
    
    private void createPlayers(List<Player> listOfPlayers){
        LOGGER.log(Level.INFO, "Initializing {0} number of players", listOfPlayers.size() );
        
        Node players = WorldCreator.createPlayers(listOfPlayers, bulletAppState, app.getAssetManager());
        
        Node playerLight = new Node();
        playerLight.attachChild(players);
        
        //Directional light that only affect player models
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        playerLight.addLight(dl);
        
        world.attachChild(playerLight);
        
        // Tell server we are ready
        gameSetupService.ready();
    }
    
}
