/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import api.models.Player;
import client.GameState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.WorldCreator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamesetup.AllReadyListener;
import network.service.gamesetup.GameSetupSession;
import network.service.gamesetup.server.HostedGameSetupService;

/**
 *
 * @author ted
 */
public class SetupState extends BaseAppState implements AllReadyListener{

    private static final Logger LOGGER = Logger.getLogger(client.SetupState.class.getName());
    
    private Node world;
    
    private AssetManager asset;
    
    private BulletAppState bulletAppState;
    
    private GameServer app;
    
    private Map<Integer, String> playerInfo;
    
    private HostedGameSetupService hostedGameSetupService;
    
    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;  
        world = new Node("world");
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
        hostedGameSetupService = app.getHostedGameSetupService();
        asset = app.getAssetManager();

        loadStaticGeometry();
    }

    public void setPlayerInfo(Map<Integer, String> playerInfo){
        this.playerInfo = playerInfo;
    }
    
    @Override
    protected void onDisable() {
//        hostedGameSetupService.removeGameSetupSessionListener(this);
    }

    public void initPlayer(List<Player> listOfPlayers) {
        
        app.enqueue(() -> {
            createPlayers(listOfPlayers);
        });
    }

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
        
        world.attachChild(players);
        
    }

    @Override
    public void notifyAllReady() {
    }
    
    
}
