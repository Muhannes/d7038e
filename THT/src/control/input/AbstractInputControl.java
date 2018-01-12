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
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import control.EntityNode;
import control.MyCharacterControl;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 * Base class for Human and Monster input control
 * 
 * @author truls
 */
public abstract class AbstractInputControl extends AbstractControl implements AnalogListener, ActionListener{
    private final Object lock = new Object();
    private final Object charLock = new Object();
    private static final Logger LOGGER = Logger.getLogger(AbstractInputControl.class.getName());
    protected final ClientMovementService movementService;
    protected final ClientGameStatsService gameStatsService;
    private boolean forward = false, backward = false, strafeLeft = false, strafeRight = false;
    
    protected MyCharacterControl character;
    private boolean isSending = false;
    private ScheduledExecutorService executor;
    
    
    public AbstractInputControl(ClientMovementService movementService, ClientGameStatsService gameStatsService){
        this.movementService = movementService; 
        this.gameStatsService = gameStatsService;
        executor = Executors.newScheduledThreadPool(1);
    }
    
    private void shutDownExecutor(){
        executor.shutdown();
    }
    
    public void closeControl(InputManager input){
        shutDownExecutor();
        disableKeys(input);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }
    
    private Vector3f setNewMoveDirection(float tpf) {
        if(character == null){
            character = getSpatial().getControl(MyCharacterControl.class);
            if(character == null){
                throw new RuntimeException("AbstractInputControl requires a CharacterControl to be attached to spatial");
            }
        }
        Vector3f newMoveDirection;
        synchronized(charLock){
            Vector3f moveDir = character.getViewDirection();
            Vector3f moveDirLeft = rotateY(moveDir, FastMath.DEG_TO_RAD * 90);
            moveDir.y = 0;
            moveDirLeft.y = 0;
            moveDir.normalizeLocal();
            moveDirLeft.normalizeLocal();
            newMoveDirection = new Vector3f(0,0,0);
            if(forward) newMoveDirection.addLocal(moveDir);
            if(backward) newMoveDirection.addLocal(moveDir.negate());
            if(strafeLeft) newMoveDirection.addLocal(moveDirLeft);
            if(strafeRight) newMoveDirection.addLocal(moveDirLeft.negate());
            character.setNextDirection(newMoveDirection);
        }
        return newMoveDirection;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(character == null){
            character = getSpatial().getControl(MyCharacterControl.class);
            if(character == null){
                throw new RuntimeException("AbstractInputControl requires a CharacterControl to be attached to spatial");
            }
        }

        if(name.equals("forward")) forward = isPressed;
        else if(name.equals("backward")) backward = isPressed;
        else if(name.equals("strafeLeft")) strafeLeft = isPressed;
        else if(name.equals("strafeRight")) strafeRight = isPressed;
        else if(name.equals("jump") && isPressed){
            character.jump();
            sendJumpToServer();
        }                
        setNewMoveDirection(tpf);
        sendMovementToServer();                    
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(character == null){
            character = getSpatial().getControl(MyCharacterControl.class);
            if(character == null){
                throw new RuntimeException("AbstractInputControl requires a CharacterControl to be attached to spatial");
            }
        }
        if(name.equals("rotateleft")){
            rotateY(-value);
            setNewMoveDirection(tpf);
            sendMovementToServer();
        }else if(name.equals("rotateright")){
            rotateY(value);
            setNewMoveDirection(tpf);
            sendMovementToServer();
        }else if(name.equals("rotateup")){
            ((Node) getSpatial()).getChild("CamNode").rotate(value, 0, 0);
        }else if(name.equals("rotatedown")){
            ((Node) getSpatial()).getChild("CamNode").rotate(-value, 0, 0);
        }
        
    }
    
    /**
     * Sends information about model to server
     */
    private void sendMovementToServer(){
        
        synchronized(lock){
            if (!isSending) {
                executor.schedule(movementSender, 20, TimeUnit.MILLISECONDS);
                isSending = true;
            }
        }
    }
    
    private void sendJumpToServer(){
        gameStatsService.notifyJump(getSpatial().getName());
    }

    private final Runnable movementSender = new Runnable(){
        @Override
        public void run(){
            PlayerMovement pm;
            synchronized(lock){
                isSending = false;
            }
            synchronized(charLock){
                Spatial self = getSpatial();
                pm = new PlayerMovement(self.getName(), self.getLocalTranslation(),
                        character.getWalkDirection(), character.getViewDirection());
            }
            movementService.sendPlayerMovement(pm);
        }
    };
    
    private void rotateY(float rotationRad){
        synchronized(charLock){
            Vector3f oldViewRot = character.getViewDirection();
            character.setViewDirection(rotateY(oldViewRot, rotationRad));
        }
    }
    
    private Vector3f rotateY(Vector3f v, float rotationRad){
        float x = (FastMath.cos(rotationRad) * v.x) + (FastMath.sin(rotationRad) * v.z);
        float z = (FastMath.cos(rotationRad) * v.z) - (FastMath.sin(rotationRad) * v.x);
        return new Vector3f(x, v.y, z);
    }
    
    
    @Override
    protected void controlUpdate(float tpf) {
        // Scale every frame, since tpf changes.
        setNewMoveDirection(tpf);
    }
    
    /**
     * Set up keybindings
     * @param manager 
     */
    public void initKeys(InputManager manager){
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("strafeLeft", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("strafeRight", new KeyTrigger(KeyInput.KEY_D));     
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        manager.addMapping("rotateright", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        manager.addMapping("rotateleft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        manager.addMapping("rotateup", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        manager.addMapping("rotatedown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        manager.addListener(this, "forward", "backward", "strafeLeft", "strafeRight", 
                "rotateleft", "rotateright", "rotateup", "rotatedown", "jump");    
    }
    
    public void disableKeys(InputManager input){       
        //Clearing human player settings
        input.deleteMapping("forward");
        input.deleteMapping("backward");
        input.deleteMapping("strafeLeft");
        input.deleteMapping("strafeRight");
        input.deleteMapping("rotateright");
        input.deleteMapping("rotateleft");
        input.deleteMapping("rotateup");
        input.deleteMapping("rotatedown");
        input.deleteMapping("jump");
    }    
}
