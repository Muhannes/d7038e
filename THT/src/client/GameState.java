/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import control.Entity;
import control.Human;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSessionListener;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.login.client.ClientLoginService;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState implements MovementSessionListener, GameStatsSessionListener{
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    private ClientApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    private ClientMovementService clientMovementService;
    private ClientGameStatsService clientGameStatsService;
    
    private MovementSessionListener movementListener;
    private GameStatsSessionListener gameStatsListener;
    
    private Node root;
    private AssetManager asset;
    private InputManager input;
    private GameGUI game;
    
    private Entity player;
    private ChaseCamera chaseCamera;
    private Camera camera;
    private Human human;
    private int id;
    
    private long slow;
    
    
    @Override
    protected void initialize(Application app) {
        
        this.app = (ClientApplication) app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        game = new GameGUI(niftyDisplay);
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
    }

    @Override
    protected void cleanup(Application app) {
        if(root != null){
            root.detachAllChildren();
        }
    }

    @Override
    protected void onEnable() {     
        movementListener = this;
        gameStatsListener = this;
        
        this.root = app.getRootNode();
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        
        /* Listeners */
        this.clientMovementService = app.getClientMovementService();        
        this.clientMovementService.addListener(movementListener);
        
        this.clientGameStatsService = app.getClientGameStatsService();
        this.clientGameStatsService.addGameStatsSessionListener(gameStatsListener);
        
        Node playerNode = (Node) root.getChild("players");
        player = (Entity) playerNode.getChild(""+ClientLoginService.getAccount().id);
        if(player == null){
            LOGGER.log(Level.SEVERE, "player is null");
        }
        if(camera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        if(input == null){
            LOGGER.log(Level.SEVERE, "inputmanager is null");
        }
        
        chaseCamera = new ChaseCamera(camera, player, input);
        chaseCamera.setMaxDistance(12);
        
        if(chaseCamera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        human = new Human(player, app, clientMovementService, clientGameStatsService);
        human.initKeys(input);               

    }

    @Override
    protected void onDisable() {        
        app.stop();
    }
    
    @Override
    public void update(float tpf){
        // Scale walking speed by tpf
        for (Spatial entity : ((Node)app.getRootNode().getChild("players")).getChildren()) {
            ((Entity) entity).scaleWalkDirection(tpf);
        }
        
        //Contantly increase walking speed if entity is slowed from previous trap.
 
        //Check if any player walks on a trap!
        
    }
    
    @Override
    public void newMessage(List<PlayerMovement> playerMovements) {        
        app.enqueue(() -> {
            convergePlayers(playerMovements);
        });
    }
    
    /**
     * converges all players that have changed their movementStatus.
     * currently only snaps. Should be linear convergence for characters that moves "short distances".
     * @param playerMovements 
     */
    private void convergePlayers(List<PlayerMovement> playerMovements){
     
        Node players = (Node) root.getChild("players");
        for (PlayerMovement playerMovement : playerMovements) {
            if (playerMovement.id.equals(player.getName())) { // This player
                LOGGER.log(Level.INFO, "Converging self: {0}", playerMovement.id);
                player.convergeSnap(playerMovement.location, player.getWalkDirection(), player.getLocalRotation());
            } else { // Other entity, converge
                LOGGER.log(Level.INFO, "Converging player: {0}", playerMovement.id);
                Entity entity = (Entity) players.getChild(playerMovement.id);
                entity.convergeSnap(playerMovement.location, playerMovement.direction, playerMovement.rotation);
            }
        }
        
    }

    public void receivedNewTrap(String name, Vector3f position){
        
    }
    
    public void triggeredTrap(String name, String trapName){
        root.detachChildNamed(trapName);
        Node playerNode = (Node) root.getChild("players");
        Entity triggerer = (Entity) playerNode.getChild(name);
        triggerer.setWalkDirection(triggerer.getWalkDirection().mult(0.0f));
    }

    
    @Override
    public void notifyPlayersKilled(List<String> victims, List<String> killers) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyPlayersEscaped(List<String> names) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapsPlaced(List<String> trapNames, List<Vector3f> newTraps) {
        for(String trapName : trapNames){
            //Create a trap at the location with the name given.
        }
    }

    @Override
    public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
        for(String name : names){
            //The players that triggered the corresponding trap is slowed.
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
