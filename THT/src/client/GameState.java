/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import control.EntityNode;
import control.HumanNode;
import control.MonsterNode;
import control.WorldCreator;
import control.audio.AmbientAudioService;
import control.audio.ListenerControl;
import control.audio.MonsterAudioControl;
import control.converge.ConvergeControl;
import control.input.AbstractInputControl;
import control.input.HumanInputControl;
import control.input.MonsterInputControl;
import gui.game.GameGUI;
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
    private ClientMovementService clientMovementService;
    private ClientGameStatsService clientGameStatsService;
    
    private GameStatsSessionListener gameStatsListener;
        
    private NiftyJmeDisplay niftyDisplay;
    private GameGUI gui;
    
    private Node root;
    private Node traps;
    private Node playerNode;
    private AssetManager asset;
    private InputManager input;
    
    private EntityNode player;
    private Camera camera;
    private CameraNode camNode;
    private int id;
    private Boolean sentGameOver;
            
            
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;
                
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
                
        /* GUI */
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        app.getInputManager().setCursorVisible(false);
        gui = new GameGUI(niftyDisplay);
        
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
        input.setCursorVisible(false);
        camNode = new CameraNode("CamNode", camera);
        // so that walls are not invisible
        camera.setFrustumPerspective(45, Display.getWidth() / Display.getHeight(), 0.23f, 1000);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        player.attachChild(camNode);        

        if (player instanceof HumanNode) {
            camNode.setLocalTranslation(new Vector3f(0, 0.3f, 0));
            HumanInputControl inputControl = new HumanInputControl(player, clientMovementService, clientGameStatsService);
            player.addControl(inputControl);
            inputControl.initKeys(input);
        } else if (player instanceof MonsterNode) {
            camNode.setLocalTranslation(new Vector3f(0, 0.7f, 0));
            MonsterInputControl inputControl = new MonsterInputControl(player, clientMovementService, clientGameStatsService);
            player.addControl(inputControl);
            inputControl.initKeys(input);
            
        }
        
        playerNode.getChildren().forEach((p) -> {
            if( p instanceof MonsterNode ){
                p.addControl(new MonsterAudioControl(app.getAssetManager()));
            }
        });
        
        player.addControl(new ListenerControl(app.getListener()));
        
        playerNode.getChildren().forEach((p) -> {   
            ConvergeControl converger;
            if(p.getName().equals(player.getName())){
                converger = new ConvergeControl(clientMovementService, false);
            }else{
                converger = new ConvergeControl(clientMovementService);
            }
            p.addControl(converger);
        });

        AmbientAudioService.getAmbientAudioService(app.getAssetManager()).playGameMusic();
        
        sentGameOver = true;
        
        if(playerNode.getChild(player.getName()) instanceof HumanNode){ //If player, show the tool tip, 'f' for trap.
            gui.displayTooltip(); 
        }

    }

    @Override
    protected void onDisable() {  
        player.getControl(AbstractInputControl.class).closeControl(input);
        AmbientAudioService.getAmbientAudioService(app.getAssetManager()).stopGameMusic();
        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().removeAll(root);
        
        root.detachAllChildren();
        app.getViewPort().removeProcessor(niftyDisplay);
        //Clean up nifty
        niftyDisplay.getNifty().exit();
        niftyDisplay.cleanup();
        niftyDisplay = null;

    }
    
    @Override
    public void update(float tpf){
        
    }
    
    @Override
    public void notifyPlayersKilled(String victim, String killer) {
        app.enqueue(() -> {            

            if(victim.equals(player.getName())){ // Myself died

                player.getControl(AbstractInputControl.class).disableKeys(input);
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victim).getControl(GhostControl.class)); //reset bulletAppState
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victim).getControl(CharacterControl.class)); //reset bulletAppState

                playerNode.detachChildNamed(victim);

                /* -------------------------------------------------------------- */

                //create monster 
                EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victim, app.getStateManager().getState(BulletAppState.class));
                newMonster.attachChild(camNode);

                //monster control
                MonsterInputControl monsterInputControl = new MonsterInputControl(newMonster, clientMovementService, clientGameStatsService);
                newMonster.addControl(monsterInputControl);
                monsterInputControl.initKeys(input);

                newMonster.addControl(new MonsterAudioControl(app.getAssetManager()));
                newMonster.addControl(new ListenerControl(app.getListener()));

                if(newMonster.getControl(ListenerControl.class) == null){
                    LOGGER.log(Level.SEVERE, "There is no listenerControl!");
                }
                
                //converge control
                ConvergeControl converge = new ConvergeControl(clientMovementService, false);
                newMonster.addControl(converge);

                //attach new monster to playground
                player = newMonster; //might be usedful for other methods.        
                playerNode.attachChild(player);
                // Adjust camera height to monster model size
                camNode.setLocalTranslation(new Vector3f(0, 0.7f, 0));

            } else {

                //reset bullet
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victim).getControl(GhostControl.class)); //reset bulletAppState
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(playerNode.getChild(victim).getControl(CharacterControl.class)); //reset bulletAppState
                //delete node
                playerNode.detachChildNamed(victim);

                //create monster
                EntityNode newMonster = WorldCreator.createMonster(app.getAssetManager(), victim, app.getStateManager().getState(BulletAppState.class));

                //monster sounds
                newMonster.addControl(new MonsterAudioControl(app.getAssetManager()));

                //attach new convergeControl
                newMonster.addControl(new ConvergeControl(clientMovementService));
                //attach new monster
                playerNode.attachChild(newMonster);                     
            }
            //Always display a death
            death();
        });
    }

    @Override
    public void notifyTrapsPlaced(String trapName, Vector3f newTrap) {
        app.enqueue(() -> {
            updateTreeWithNewTraps(trapName, newTrap);
        });    
    }

    public void updateTreeWithNewTraps(String trapName, Vector3f newTrap){
        if(traps.getChild(trapName) == null){    
            Spatial trap = asset.loadModel("Models/trap/trap.j3o");

            Vector3f position = newTrap;
            trap.setLocalTranslation(position);        

            //Create node for each Trap (Only server needs to control check ghosts)
            Node node = new Node(trapName);
            node.attachChild(trap);
            traps.attachChild(node);

            if(player instanceof HumanNode){
                HumanNode human = (HumanNode) player;
                gui.displayTraps(human.getInput().getTrapRemaining());
            }

        }
    }
    
    @Override
    public void notifyTrapsTriggered(String name, String trapName) {
        app.enqueue(() -> {
            updateTreeWithDeletedTraps(name, trapName);
        });    
    }
    
    public void updateTreeWithDeletedTraps(String name, String trapName){
        traps.detachChildNamed(trapName);
        EntityNode entity = (EntityNode) playerNode.getChild(name);
        entity.slowDown();
    }

    @Override
    public void notifyMonkeysCaught(String catcher, String monkey) {
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                Spatial m = playerNode.getChild(monkey);
                if(m == null){
                    LOGGER.severe("monkey does not exist");
                } else {
                    //reset the monkey bullet
                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(m.getControl(GhostControl.class)); //reset bulletAppState
                    app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(m.getControl(CharacterControl.class)); //reset bulletAppState

                    //remove monkey
                    playerNode.detachChildNamed(monkey);

                    //Do something with all the catchers (send out to GUI)
                    caught();
                }
            }
        });        
    }

    @Override
    public void notifyGameOver(String winner) {
        if(sentGameOver){
            GameState gs = this;
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    gs.setEnabled(false);
                    app.getStateManager().getState(GameOverState.class).setEnabled(true);
                    app.getStateManager().getState(GameOverState.class).setWinner(winner);
                }
            });   
            sentGameOver = false;
        }
    }

    public void death() {
        gui.displayKiller();
    }

    public void caught() {
        gui.displayCatch();
    }

    @Override
    public void notifyPlayerJumped(String name) {
        //Received that a player has jumped
        if(!name.equals(this.player.getName())){
            if(playerNode.getChild(name) instanceof EntityNode){
                EntityNode entity = (EntityNode) playerNode.getChild(name);
                entity.jumped();
            }            
        }
    }

    @Override
    public void notifyPlayerSlashed(String name) {
        //Received that a player has slashed
        if(!name.equals(this.player.getName())){
            if(playerNode.getChild(name) instanceof MonsterNode){
                MonsterNode monster = (MonsterNode) playerNode.getChild(name);
                if(monster.getmodel() == null){
                    LOGGER.log(Level.INFO, "model is null");
                }
                if(monster.getAnimation() == null){
                    LOGGER.log(Level.INFO, "animation is null");                    
                }else{
                    monster.getAnimation().swordSlash();
                }
            }
        }
    }
}
