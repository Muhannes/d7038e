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
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
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
        
        Account acc = ClientLoginService.getAccount();
        gameSetupService.join(acc.id, acc.key, acc.name);
            
        //Bullet physics for players, walls, objects
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);  
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
            players.attachChild(createPlayer("player#"+Integer.toString(p.getID()), p.getPosition()));
        });
        
        // Tell server we are ready
        gameSetupService.ready();
    }

    @Override
    public void startGame() {
        System.out.println("Changing to gamestate!");
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
            RigidBodyControl b = new RigidBodyControl(
                   CollisionShapeFactory.createBoxShape(wall), 0); // 0 Mass = static
            
            b.setKinematic(true); // This for some reason makes the rigid align with the Mesh...
            
            wall.addControl(b);  
            
            bulletAppState.getPhysicsSpace().add(b);  
        });
        
        Spatial floors = ((Node)creepyhouse).getChild("floor");
        ((Node)floors).getChildren().forEach((floor) -> {
            RigidBodyControl b = new RigidBodyControl(0); // 0 Mass = static
            
            floor.addControl(b);

            bulletAppState.getPhysicsSpace().add(b);
        });
        
        LOGGER.log(Level.INFO, "Number of walls: {0}, Number of floors: {1}", 
                new Object[]{((Node)walls).getChildren().size(), ((Node)floors).getChildren().size()});
    }
    
    private Spatial createPlayer(String name, Vector3f position){
        LOGGER.log(Level.INFO, "Name: {0}, Position: {1}", new Object[]{name, position.toString()});
        
        Box mesh = new Box(0.2f, 0.4f, 0.2f);
        Geometry player = new Geometry(name, mesh);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        player.setMaterial(mat);
        player.setLocalTranslation(new Vector3f(-5.5f,5f, -9.5f));
        
        BoundingBox boundingBox = (BoundingBox) player.getWorldBound();
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);                
        CharacterControl charControl = new CharacterControl(shape, 1.0f); 
        player.addControl(charControl);
        
        if(bulletAppState == null){
            LOGGER.log(Level.SEVERE, "BulletAppState is null");   
            
        }
        
        if(bulletAppState.getPhysicsSpace() == null){
            LOGGER.log(Level.SEVERE, "physicsSpace is null");
        }
        
        bulletAppState.getPhysicsSpace().add(charControl);
        
        return player;
    }
    
}
