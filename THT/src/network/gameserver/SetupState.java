/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import api.models.EntityType;
import api.models.Player;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.WorldCreator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamesetup.AllReadyListener;
import network.service.gamesetup.GameSetupSessionListener;
import network.service.gamesetup.server.HostedGameSetupService;
import network.service.login.Account;
import network.service.movement.server.HostedMovementService;

/**
 *
 * @author ted
 */
public class SetupState extends BaseAppState implements AllReadyListener{

    private static final Logger LOGGER = Logger.getLogger(client.SetupState.class.getName());
    
    private static final Random RANDOM = new Random();

    private Node world;    
    private AssetManager asset;
    private BulletAppState bulletAppState;
    private GameServer app;
    private HostedGameSetupService hostedGameSetupService;
    private HostedMovementService hostedMovementService;
    private List<Account> accounts;
    
    private final int MONKEYS_PER_PLAYER = 2;
    
    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;  
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
        LOGGER.info("SETUP STATE ENABLED");
        world = new Node("world");
        LOGGER.log(Level.INFO, "world node : " + world);
        this.app.getRootNode().attachChild(world);
        hostedGameSetupService = app.getHostedGameSetupService();
        asset = app.getAssetManager();
        hostedGameSetupService.addListener(this);
        List<Player> playerInits = createPlayerInitInfo();
        hostedGameSetupService.setInitialized(playerInits, accounts);
        loadStaticGeometry();
        createPlayers(playerInits);
    }
    
    public void setAccounts(List<Account> accounts){
        this.accounts = accounts;
    }
    
    @Override
    protected void onDisable() {
    }

    public void initPlayer(List<Player> listOfPlayers) {
        
        app.enqueue(() -> {
            createPlayers(listOfPlayers);
        });
    }
    
    private void loadStaticGeometry(){   
        Spatial creepyhouse = asset.loadModel("Scenes/creepyhouse.j3o");
        world.attachChild(creepyhouse);   
        
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
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        Node players = WorldCreator.createPlayers(listOfPlayers, bulletAppState, app.getAssetManager());
        for(Spatial player : players.getChildren()){            
            GhostControl ghost = new GhostControl(new BoxCollisionShape(new Vector3f(0.5f,1f,0.5f))); //test vector
            player.addControl(ghost);            
            bulletAppState.getPhysicsSpace().add(ghost);
        }

        players.setName("playersNode");
        
        world.attachChild(players);        
    }
    
    /**
     * When accounts are received, run this.
     * Creates Player objects for each participant.
     */
    private List<Player> createPlayerInitInfo(){
        List<Player> players = new ArrayList<>();
        Random random = new Random(); 

        LOGGER.log(Level.INFO, "Number of players in game: {0}", accounts.size());
        int monsterID = random.nextInt(accounts.size());

        for (int a = 0; a < accounts.size(); a++) {
            if(a == monsterID){
                players.add(new Player(EntityType.Monster, 
                    new Vector3f(-4.866537f, 5.9999995f, -16.220175f), new Quaternion(0, 0, 0, 0), accounts.get(a).id));  
            }else{
                players.add(new Player(EntityType.Human, 
                    new Vector3f(16.662006f, 6.0000014f, 7.364316f), new Quaternion(0, 0, 0, 0), accounts.get(a).id)); 
            }            
        }
        players.get(monsterID).setType(EntityType.Monster);
        
        // Add Two monkeys for every player in game (Humans + Monster)
        int size = players.size();
        for(int m = 0; m < MONKEYS_PER_PLAYER * size; m++){
            players.add(new Player(EntityType.Monkey, new Vector3f(0,2,0), new Quaternion(0, 0, 0, 0), -m));            
        }
        return players;
    }

    public void startGame() {
        SetupState ss = this;
        app.enqueue(() -> {
            ss.setEnabled(false);
            app.getStateManager().getState(PlayState.class).setEnabled(true);
        });         
    }

    @Override
    public void notifyAllReady() {
        startGame();
    }
    
}
