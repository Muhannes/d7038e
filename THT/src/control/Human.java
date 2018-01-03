/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import control.action.Jump;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.List;
import java.util.logging.Level;
import com.sun.istack.internal.logging.Logger;
import net.java.games.input.Component;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class Human extends AbstractController implements ActionListener, AnalogListener{

    private static final Logger LOGGER = Logger.getLogger(Human.class);

    private Jump jump;
    
    private int numberOfTraps = 5; 
    
    public Boolean forward = false, backward = false, left = false, right = false, strafeLeft = false, strafeRight = false;
    public Boolean stopped = true;
    
    private final EntityNode self;
    private final AssetManager asset;
    private final SimpleApplication app;
    private Camera camera;
    
    
    private final ClientMovementService clientMovementService;
    private final ClientGameStatsService clientGameStatsService;
    
    public Human(EntityNode player, SimpleApplication app, ClientMovementService clientMovementService, ClientGameStatsService clientGameStatsService){
        this.self = player;
        this.app = (SimpleApplication)app;
        this.asset = app.getAssetManager();
        this.camera = app.getCamera();
        this.clientMovementService = clientMovementService;
        this.clientGameStatsService = clientGameStatsService;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("jump")){ 
            self.charControl.jump();
        }
        if(name.equals("forward")){
            if(isPressed){
                forward = true;
            }else{
                forward = false;
            }
        }
        if(name.equals("backward")){
            if(isPressed){
                backward = true;
            }else{
                backward = false;
            }
        }
        if(name.equals("strafeLeft")){
            if(isPressed){
                strafeLeft = true;
            }else{
                strafeLeft = false;
            }
        }
        if(name.equals("strafeRight")){
            if(isPressed){
                strafeRight = true;
            }else{
                strafeRight = false;
            }
        } 
        if(name.equals("left")){
            if(isPressed){
                left = true;
            }else{
                left = false;
            }
        }
        if(name.equals("right")){
            if(isPressed){
                right = true;
            }else{
                right = false;
            }
        }        
        if(!forward && !backward && !strafeLeft && !strafeRight && !left && !right){
            stopped = true;
        } else {
            stopped = false;
        }
        setNewMoveDirection();
        sendMovementToServer();
        
        if(name.equals("trap")){
            if(isPressed){                
                createTrap();
            }
        }           
    }
    
    /**
     * Depending on what buttons are pressed, set a new movementDirection
     * Should always be called when updating speed or direction.
     */
    private void setNewMoveDirection(){
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        Vector3f moveDirection = new Vector3f(Vector3f.ZERO);
        if (forward) {
            moveDirection.addLocal(camDir);
        }
        if (backward) {
            moveDirection.addLocal(camDir.negate());
        }
        if (strafeLeft) {
            moveDirection.addLocal(camLeft);
        }
        if (strafeRight) {
            moveDirection.addLocal(camLeft.negate());
        }
        if(left) {
            System.out.println("self rotation : " + self.getLocalRotation());
            Quaternion tmp = self.getLocalRotation();
        }
        if(right) {
            System.out.println("self rotation : " + self.getLocalRotation());            
        }
        
        moveDirection.normalizeLocal();
        moveDirection.multLocal(self.movementSpeed);

        self.setWalkDirection(moveDirection);
    }
    
    /**
     * Sends information about model to server
     */
    private void sendMovementToServer(){         
        PlayerMovement pm = new PlayerMovement(self.getName(), self.getLocalTranslation(),
                self.getWalkDirection(), self.getViewDirection());
        clientMovementService.sendMessage(pm);
    }
    
    public void createTrap(){
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
        */
            Vector3f position = self.getLocalTranslation();
            position.y = 0.1f;
            sendTrapToServer(trapName, position);
            LOGGER.log(Level.INFO, "trying to send trap to server");
            numberOfTraps--;
        }
    }
    
    /**
     * Send trap information to server
     */
    private void sendTrapToServer(String trapName, Vector3f newTrap){
        LOGGER.log(Level.INFO, "sending new trap to server");
        clientGameStatsService.notifyTrapPlaced(trapName, newTrap);
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
        manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "trap", "rotateright", "rotateleft");
    }

    /**
     * TODO: makes rotations
     * @param name
     * @param value
     * @param tpf 
     */
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(name.equals("rotateleft")){
            self.rotateY(-value);
            setNewMoveDirection();
            sendMovementToServer();
        }
        if(name.equals("rotateright")){
            self.rotateY(value);
            setNewMoveDirection();
            sendMovementToServer();
            
        }
    }
}
