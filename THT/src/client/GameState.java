/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import static api.models.EntityType.Monster;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import control.EntityNode;
import control.converge.ConvergeControl;
import control.input.HumanInputControl;
import de.lessvoid.nifty.Nifty;
import gui.game.GameGUI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.gamestats.GameStatsSessionListener;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.login.client.ClientLoginService;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState implements GameStatsSessionListener{
    private static final Logger LOGGER = Logger.getLogger(GameState.class.getName());
    private ClientApplication app;
    private NiftyJmeDisplay niftyDisplay; 
    private Nifty nifty;
    private ClientMovementService clientMovementService;
    private ClientGameStatsService clientGameStatsService;
    
    private GameStatsSessionListener gameStatsListener;
        
    private Node root;
    private Node traps;
    private Node playerNode;
    private AssetManager asset;
    private InputManager input;
    private GameGUI game;
    
    private EntityNode player;
    private ChaseCamera chaseCamera;
    private Camera camera;
    private int id;
        
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
        gameStatsListener = this;
        
        this.root = app.getRootNode();   
        this.asset = app.getAssetManager();
        this.input = app.getInputManager();
        this.camera = app.getCamera();
        
        /* Listeners */
        this.clientMovementService = app.getClientMovementService();      
        
        this.clientGameStatsService = app.getClientGameStatsService();
        this.clientGameStatsService.addGameStatsSessionListener(gameStatsListener);
        
        this.traps = (Node) app.getRootNode().getChild("traps");         
        this.playerNode = (Node) root.getChild("players");
        this.player = (EntityNode) playerNode.getChild(""+ClientLoginService.getAccount().id);

        if(player == null){
            LOGGER.log(Level.SEVERE, "player is null");
        }
        if(camera == null){
            LOGGER.log(Level.SEVERE, "Camera is null");
        }
        if(input == null){
            LOGGER.log(Level.SEVERE, "inputmanager is null");
        }
        
        // set forward camera node that follows the character
        CameraNode camNode = new CameraNode("CamNode", camera);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 1, 0));
        //camNode.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
        player.attachChild(camNode);
        //chaseCamera = new ChaseCamera(camera, player, input);
        //chaseCamera.setMaxDistance(0);
        //chaseCamera.setDefaultDistance(0);
        
        if(chaseCamera == null){
            LOGGER.log(Level.SEVERE, "chaseCamera is null");
        }
        HumanInputControl inputControl = new HumanInputControl(player, clientMovementService, clientGameStatsService, camera);
        player.addControl(inputControl);
        inputControl.initKeys(input);  
        
        playerNode.getChildren().forEach((p) -> {   
            ConvergeControl converger = new ConvergeControl(clientMovementService);
            p.addControl(converger);
        });

    }

    @Override
    protected void onDisable() {        
        app.stop();
    }
    
    @Override
    public void update(float tpf){
        
    }
    
    @Override
    public void notifyPlayersKilled(List<String> victims, List<String> killers) {
        LOGGER.log(Level.INFO, "Victim list : " + victims + "\nKiller list : " + killers);
/*        for(int i = 0; i < victims.size(); i++){
           //Print out to GUI that killer slaughtered the victim
            if(victims.get(i).equals(player.getName())){
               //TODO: change the player to new monsterNode and change position

                // set forward camera node that follows the character
                CameraNode camNode = new CameraNode("CamNode", camera);
                camNode.setControlDir(ControlDirection.SpatialToCamera);
                camNode.setLocalTranslation(new Vector3f(0, 1, 0));
                player.attachChild(camNode);

                if(chaseCamera == null){
                    LOGGER.log(Level.SEVERE, "chaseCamera is null");
                }
                monster = new Monster(player, app, clientMovementService, clientGameStatsService);
                monster.initKeys(input); 

//                playerNode.detachChildNamed(victims.get(i));
//                EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victims.get(i), app.getStateManager().getState(BulletAppState.class));
//                playerNode.attachChild(newMonster);
//               //TODO: Set new camera
            } else {
             //TODO: change the player to new monsterNode and change position
                playerNode.detachChildNamed(victims.get(i));
                EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victims.get(i), app.getStateManager().getState(BulletAppState.class));
                playerNode.attachChild(newMonster);
           }
        }*/
    }

    @Override
    public void notifyPlayersEscaped(List<String> names) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyTrapsPlaced(List<String> trapNames, List<Vector3f> newTraps) {
        app.enqueue(() -> {
            updateTreeWithNewTraps(trapNames, newTraps);
        });    
    }

    public void updateTreeWithNewTraps(List<String> trapNames, List<Vector3f> newTraps){
        for(int i = 0; i < trapNames.size(); i++){
            if(traps.getChild(trapNames.get(i)) == null){
                //Create a trap at the location with the name given.
                Box box = new Box(0.1f,0.1f,0.1f);
                Geometry geom = new Geometry(trapNames.get(i), box);
                Material material = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
                material.setColor("Color", ColorRGBA.Red);
                geom.setMaterial(material);

                Vector3f position = newTraps.get(i);
                position.y = 0.1f;
                geom.setLocalTranslation(position);        

                //Create node for each Trap (Only server needs to control check ghosts)
                Node node = new Node(trapNames.get(i));
                node.attachChild(geom);
                traps.attachChild(node);
            }
        }
    }
    
    @Override
    public void notifyTrapsTriggered(List<String> names, List<String> trapNames) {
        app.enqueue(() -> {
            updateTreeWithDeletedTraps(names, trapNames);
        });    
    }
    
    public void updateTreeWithDeletedTraps(List<String> names, List<String> trapNames){
        List<String> updatedNames = new ArrayList<>();
        List<String> updatedTrapNames = new ArrayList<>();

        for(int i = 0; i < trapNames.size(); i++){
            if(!updatedTrapNames.contains(trapNames.get(i))){
                traps.detachChildNamed(trapNames.get(i));
                updatedTrapNames.add(trapNames.get(i));
                LOGGER.log(Level.INFO, trapNames.get(i) + " is deattached.");
            }
        }

        for(int j = 0; j < names.size(); j++){
            if(!updatedNames.contains(names.get(j))){
                updatedNames.add(names.get(j));
                EntityNode entity = (EntityNode) playerNode.getChild(names.get(j));
                entity.slowDown();
            }
        }        
    }
}
