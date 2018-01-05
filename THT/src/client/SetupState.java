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
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
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
        
        
        //Create a static node for traps
        Node traps = new Node("traps");
        app.getRootNode().attachChild(traps);
        
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
