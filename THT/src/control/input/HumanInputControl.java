/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;  
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;
import com.sun.istack.internal.logging.Logger;
import control.EntityNode;
import java.util.logging.Level;
/**
 * This control handles how a human should react to keyboard input.
 * 
 * @author truls
 * @author ted
 * @author hannes
 */
public class HumanInputControl extends AbstractInputControl{
    
    private static final Logger LOGGER = Logger.getLogger(HumanInputControl.class);

    private CharacterControl character;

    // Physical body that we use to control movment of spatial
    
    private Camera camera;
    // Camera chasing the player
    
    private Vector3f moveDirection;
    // Used to set walking direction
    
    private Vector3f camDir;
    // Used to set new movement direction
    
    private Vector3f camLeft;
    // Used to set new movement direction
    
    private final float updatePeriod = 0.1f;
    private float lastUpdate = 0f;
    
    private EntityNode self;
    private int traps = 5;

    public HumanInputControl(EntityNode self, ClientMovementService movementService, ClientGameStatsService gameStatsService, Camera camera) {
        super(movementService, gameStatsService); 
        this.self = self;
        this.camera = camera;
        this.moveDirection = new Vector3f(0, 0, 0);
    }
    
    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("left", new KeyTrigger(KeyInput.KEY_Q));
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("right", new KeyTrigger(KeyInput.KEY_E));
        manager.addMapping("strafeLeft", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("strafeRight", new KeyTrigger(KeyInput.KEY_D));        
        manager.addMapping("trap", new KeyTrigger(KeyInput.KEY_F));        
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        manager.addMapping("rotateright", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        manager.addMapping("rotateleft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "trap", "rotateleft", "rotateright");
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        
        moveDirection.normalizeLocal().multLocal(((EntityNode)getSpatial()).movementSpeed * tpf);
        if(character != null) {
            character.setWalkDirection(moveDirection);
            //character.setViewDirection(moveDirection);
            
            lastUpdate += tpf;
            if(lastUpdate > updatePeriod){
                sendMovementToServer();
                lastUpdate -= updatePeriod;
            }
        }
        moveDirection = new Vector3f(0, 0, 0);
        
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(character == null){
            character = getSpatial().getControl(CharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharacterControl to be attached to spatial");
            }
        }
        
        if(name.equals("rotateleft")){
            rotateY(-value);
            //sendMovementToServer();
        }
        
        if(name.equals("rotateright")){
            rotateY(value);
            //sendMovementToServer();          
        }
        
        camDir = camera.getDirection().clone();
        camLeft = camera.getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        if(name.equals("forward")) moveDirection.addLocal(camDir);
        else if(name.equals("backward")) moveDirection.addLocal(camDir.negate());
        else if(name.equals("strafeLeft")) moveDirection.addLocal(camLeft);
        else if(name.equals("strafeRight")) moveDirection.addLocal(camLeft.negate()); 
        
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(character == null){
            character = getSpatial().getControl(CharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharacterControl to be attached to spatial");
            }
        }
        
        if(name.equals("jump") && isPressed){
           character.jump();
        }
        if(name.equals("trap") && isPressed){
            if(traps > 0){
                sendTrapsToServer();
            }
        }
    }
    
    private void sendTrapsToServer(){
        LOGGER.log(Level.INFO, "new trap sent to server");

        Vector3f position = self.getLocalTranslation();
        position.y = 0.1f;
        self.setLocalTranslation(position);
        String trapName = self.getName() + ":" + traps;
        gameStatsService.notifyTrapPlaced(trapName, position);
    }
    
    /**
     * Sends information about model to server
     */
    private void sendMovementToServer(){         
        Spatial self = getSpatial();
        PlayerMovement pm = new PlayerMovement(self.getName(), self.getLocalTranslation(),
                character.getWalkDirection(), character.getViewDirection());
        movementService.sendPlayerMovement(pm);
    }
    
    private void rotateY(float rotationRad){
        Vector3f oldRot = character.getViewDirection();
        float x = (FastMath.cos(rotationRad) * oldRot.x) + (FastMath.sin(rotationRad) * oldRot.z);
        float z = (FastMath.cos(rotationRad) * oldRot.z) - (FastMath.sin(rotationRad) * oldRot.x);
        character.setViewDirection(new Vector3f(x, oldRot.y, z));
    }
   /* 
    private void createTrap(){
        if(this.numberOfTraps > 0){
            String trapName = self.getName()+":"+this.numberOfTraps;
            /*
            //Kanske inte behövs?
            Box box = new Box(0.1f,0.1f,0.1f);
            Geometry geom = new Geometry(self.getName()+":"+this.numberOfTraps, box);
            this.numberOfTraps--;
            Material material = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Red);
            geom.setMaterial(material);
            Vector3f position = self.getLocalTranslation();
            position.y = 0.1f;
            geom.setLocalTranslation(position);
            
            Node trap = new Node(geom.getName());
            trap.attachChild(geom);
            
            GhostControl ghost = new GhostControl(new BoxCollisionShape(new Vector3f(0.1f,0.1f,0.1f)));
            trap.addControl(ghost);
            Node traps = (Node) app.getRootNode().getChild("traps");
            traps.attachChild(trap);
            
            sendTrapToServer(geom.getName(), position);
        
            Vector3f position = self.getLocalTranslation();
            position.y = 0.1f;
            sendTrapToServer(trapName, position);
            LOGGER.log(Level.INFO, "trying to send trap to server");
            numberOfTraps--;
        }
    }
    
    /**
     * Send trap information to server
     
    private void sendTrapToServer(String trapName, Vector3f newTrap){
        //LOGGER.log(Level.INFO, "sending new trap to server");
        clientGameStatsService.notifyTrapPlaced(trapName, newTrap);
    }
*/
}
