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
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
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
import com.sun.scenario.Settings;
import control.EntityNode;
import control.HumanNode;
import control.MonsterNode;
import control.WorldCreator;
import control.converge.ConvergeControl;
import control.input.HumanInputControl;
import control.input.MonsterInputControl;
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
import org.lwjgl.opengl.Display;

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
        LOGGER.log(Level.INFO, "enabling client");

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
        // so that walls are not invisible
        camera.setFrustumPerspective(45, Display.getWidth() / Display.getHeight(), 0.25f, 1000);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 1, 0));
        player.attachChild(camNode);        

        if (player instanceof HumanNode) {
            HumanInputControl inputControl = new HumanInputControl(player, clientMovementService, clientGameStatsService);
            player.addControl(inputControl);
            inputControl.initKeys(input);
        } else if (player instanceof MonsterNode) {
            MonsterInputControl inputControl = new MonsterInputControl(player, clientMovementService, clientGameStatsService);
            player.addControl(inputControl);
            inputControl.initKeys(input);
            
        }
        
        playerNode.getChildren().forEach((p) -> {   
            ConvergeControl converger;
            if(p.getName().equals(player.getName())){
                converger = new ConvergeControl(clientMovementService, false);
            }else{
                converger = new ConvergeControl(clientMovementService);
            }
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
        LOGGER.log(Level.INFO, "\nVictim list : " + victims + "\nKiller list : " + killers);
        app.enqueue(() -> {            
            for(int i = 0; i < victims.size(); i++){
                //TODO: Print out to GUI that killer slaughtered the victim
                
                if(victims.get(i).equals(player.getName())){
                    LOGGER.log(Level.INFO, "you have died!");
                    
                    //bullet reset and player removal
                    input.clearMappings();
                    if(input.hasMapping("trap")){
                        LOGGER.log(Level.SEVERE, "mapping not removed");
                    }
                    LOGGER.log(Level.SEVERE, "Clearing mappings via inputManager");

                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victims.get(i)).getControl(GhostControl.class)); //reset bulletAppState
                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victims.get(i)).getControl(CharacterControl.class)); //reset bulletAppState
                    LOGGER.log(Level.SEVERE, "Clearing character and ghost control from bullet");

                    //put this on the new monster
                    playerNode.detachChildNamed(victims.get(i));
                    LOGGER.log(Level.SEVERE, "deleting character from playerNode");

                    //new camera
                    Camera newCamera = app.getCamera();
                    CameraNode camNode = new CameraNode("CamNode", newCamera);
                    // so that walls are not invisible
                    newCamera.setFrustumPerspective(45, Display.getWidth() / Display.getHeight(), 0.25f, 1000);
                    camNode.setControlDir(ControlDirection.SpatialToCamera);
                    camNode.setLocalTranslation(new Vector3f(0, 1, 0));
                    LOGGER.log(Level.INFO, "camera set");
                    
                    //create monster 
                    EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victims.get(i), app.getStateManager().getState(BulletAppState.class));
                    newMonster.attachChild(camNode);
                    LOGGER.log(Level.SEVERE, "Created monster and set camNode");

                    //monster control
                    MonsterInputControl monsterInputControl = new MonsterInputControl(newMonster, clientMovementService, clientGameStatsService);
                    newMonster.addControl(monsterInputControl);
                    monsterInputControl.initKeys(input);
                    LOGGER.log(Level.INFO, "Created monster inputControl");
                    
                    //converge control
                    ConvergeControl converge = new ConvergeControl(clientMovementService, false);
                    newMonster.addControl(converge);
                    
                    //attach new monster to playground
                    player = newMonster; //might be usedful for other methods.
                    playerNode.attachChild(player);
                    LOGGER.log(Level.INFO, "created monster direction : " + playerNode.getChild(victims.get(i)).getControl(CharacterControl.class).getWalkDirection()); 

                } else {
                    LOGGER.log(Level.INFO, victims.get(i) + " has died by the hands of " + killers.get(i));

                    //reset bullet
                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victims.get(i)).getControl(GhostControl.class)); //reset bulletAppState
                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victims.get(i)).getControl(CharacterControl.class)); //reset bulletAppState
                    //delete node
                    playerNode.detachChildNamed(victims.get(i));
                    
                    //create monster
                    EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victims.get(i), app.getStateManager().getState(BulletAppState.class));
                    
                    //attach new convergeControl
                    newMonster.addControl(new ConvergeControl(clientMovementService));
                    //attach new monster
                    playerNode.attachChild(newMonster);                    
                    
                    LOGGER.log(Level.INFO, "Created monster : " + newMonster.getName() + " at " + newMonster.getLocalTranslation());

               }
            }
        });
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
