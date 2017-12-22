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
    private Map<Integer, String> playerInfo;
    private HostedGameSetupService hostedGameSetupService;
    private HostedMovementService hostedMovementService;
    
    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;  
        world = new Node("world");
        LOGGER.log(Level.INFO, "world node : " + world);
        this.app.getRootNode().attachChild(world);
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
        hostedGameSetupService = app.getHostedGameSetupService();
        asset = app.getAssetManager();
        hostedGameSetupService.addListener(this);
        
        List<Player> playerInits = createPlayerInitInfo(playerInfo);
        hostedGameSetupService.setInitialized(playerInits);
        loadStaticGeometry();
        createPlayers(playerInits);
    }

    public void setPlayerInfo(Map<Integer, String> playerInfo){
        this.playerInfo = playerInfo;
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
        if (bulletAppState != null) {
            WorldCreator.addPhysicsToMap(bulletAppState, creepyhouse);
        } else {
            LOGGER.severe("bulletAppState Was null when initializing world");
        }
    }
    
    private void createPlayers(List<Player> listOfPlayers){
        LOGGER.log(Level.INFO, "Initializing {0} number of players", listOfPlayers.size() );
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        
        Node players = WorldCreator.createPlayers(listOfPlayers, bulletAppState, mat);
        players.setName("playersNode");
        
        world.attachChild(players);        
    }
    
    /**
     * When expected players are received, run this.
     * Creates Player objects for each participant.
     */
    private List<Player> createPlayerInitInfo(Map<Integer, String> expectedPlayers){
        List<Player> players = new ArrayList<>();
        Random random = new Random();
        for (Integer id : expectedPlayers.keySet()) {
            players.add(new Player(EntityType.Human, new Vector3f(-random.nextInt(20),2,0), new Quaternion(0, 0, 0, 0), id));
        }
        LOGGER.log(Level.INFO, "Number of players in game: {0}", players.size());
        int monsterID = RANDOM.nextInt(players.size());
        players.get(monsterID).setType(EntityType.Monster);
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
