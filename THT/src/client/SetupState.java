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
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.gamesetup.ClientGameSetupService;
import network.services.gamesetup.GameSetupSessionListener;

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
    
    //private Node world;
    
    private AssetManager asset;
    
    private BulletAppState bulletAppState;
    
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;  
        world = new Node("world");
        this.app.getRootNode().attachChild(world);
    }

    @Override
    protected void cleanup(Application app) {
        if(world != null){ // NOTE: Should this be done here or at some higher level?
            world.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {
        gameSetupService = app.getGameSetupService();
        asset = app.getAssetManager();
        
        // Will notify us when when eveyone is ready
        gameSetupService.addGameSetupSessionListener(this);
        
        // Joining the game server
        //TODO: gameSetupService.join(Account.getGlobalID());
        gameSetupService.join(0); // REMOVE THIS 
            
        //Bullet physics for players, walls, objects
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);  
        app.getStateManager().attach(bulletAppState);
        
        loadStaticGeometry();       
    }

    @Override
    protected void onDisable() {
        gameSetupService.removeGameSetupSessionListener(this);
    }

    @Override
    public void initPlayer(List<Player> listOfPlayers) {
        LOGGER.log(Level.INFO, "Initializing {0} number of players", listOfPlayers.size() );
        
        Node players = new Node("players");
        
        world.attachChild(players);
        
        listOfPlayers.forEach(p -> {
            world.attachChild(createPlayer(Integer.toString(p.getID()), p.getPosition()));
        });
        
        LOGGER.log(Level.INFO, "BEFORE CRASH");
        // Tell server we are ready
        gameSetupService.ready();
    }

    @Override
    public void startGame() {
        SetupState ss = this;
        app.enqueue(() -> {
            ss.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);
        });         
    }
    
    private void loadStaticGeometry(){   
        Spatial creepyhouse = asset.loadModel("Scenes/creepyhouse.j3o");
        
        world.attachChild(creepyhouse);   
        
        Spatial walls = ((Node)creepyhouse).getChild("walls");
        
        ((Node)walls).getChildren().forEach((wall) -> {
            RigidBodyControl b = new RigidBodyControl();
            wall.addControl(b);
            bulletAppState.getPhysicsSpace().add(b);
        });
        
        Spatial floors = ((Node)creepyhouse).getChild("floor");
        ((Node)floors).getChildren().forEach((floor) -> {
            RigidBodyControl b = new RigidBodyControl();
            floor.addControl(b);
            bulletAppState.getPhysicsSpace().add(b);
        });
        
        LOGGER.log(Level.INFO, "Number of walls: {0}, Number of floors: {1}", 
                new Object[]{((Node)walls).getChildren().size(), ((Node)floors).getChildren().size()});
    }
    
    private Spatial createPlayer(String name, Vector3f position){
        LOGGER.log(Level.INFO, "Name: {0}, Position: {1}", new Object[]{name, position.toString()});
        
        Spatial player = new Geometry(name);
        player.setLocalTranslation(position);
        
        RigidBodyControl b = new RigidBodyControl();
        player.addControl(b);
        bulletAppState.getPhysicsSpace().add(b);
        
        return player;
    }
    
}
