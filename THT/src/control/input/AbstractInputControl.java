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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.sun.istack.internal.logging.Logger;
import control.EntityNode;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 * Base class for Human and Monster input control
 * 
 * @author truls
 */
public abstract class AbstractInputControl extends AbstractControl implements AnalogListener, ActionListener{
    
    private static final Logger LOGGER = Logger.getLogger(AbstractInputControl.class);
    protected final ClientMovementService movementService;
    protected final ClientGameStatsService gameStatsService;
    private boolean forward = false, backward = false, strafeLeft = false, strafeRight = false;
    
    
    private final float updatePeriod = 0.1f;
    private float lastUpdate = 0f;

    protected CharacterControl character;
    
    public AbstractInputControl(ClientMovementService movementService, ClientGameStatsService gameStatsService){
        this.movementService = movementService; 
        this.gameStatsService = gameStatsService;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }
    
    private void setNewMoveDirection(float tpf) {
        if(character == null){
            character = getSpatial().getControl(CharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharacterControl to be attached to spatial");
            }
        }
        Vector3f moveDir = character.getViewDirection().clone();
        Vector3f moveDirLeft = rotateY(moveDir, FastMath.DEG_TO_RAD * 90);
        moveDir.y = 0;
        moveDirLeft.y = 0;
        moveDir.normalizeLocal();
        moveDirLeft.normalizeLocal();
        Vector3f newMoveDirection = new Vector3f(0,0,0);
        if(forward) newMoveDirection.addLocal(moveDir);
        if(backward) newMoveDirection.addLocal(moveDir.negate());
        if(strafeLeft) newMoveDirection.addLocal(moveDirLeft);
        if(strafeRight) newMoveDirection.addLocal(moveDirLeft.negate());
        newMoveDirection.normalizeLocal().multLocal(((EntityNode)getSpatial()).movementSpeed * tpf);
        character.setWalkDirection(newMoveDirection);
        
    }
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(character == null){
            character = getSpatial().getControl(CharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharacterControl to be attached to spatial");
            }
        }
        
        if(name.equals("forward")) forward = !forward;
        else if(name.equals("backward")) backward = !backward;
        else if(name.equals("strafeLeft")) strafeLeft = !strafeLeft;
        else if(name.equals("strafeRight")) strafeRight = !strafeRight;
        else if(name.equals("jump") && isPressed){
           character.jump();
        }
        setNewMoveDirection(tpf);
        sendMovementToServer();
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
        
        setNewMoveDirection(tpf);
        sendMovementToServer();
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
        Vector3f oldViewRot = character.getViewDirection();
        character.setViewDirection(rotateY(oldViewRot, rotationRad));
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
        
        // Comment out to send updates to server regularly
        /*lastUpdate += tpf;
        if(lastUpdate > updatePeriod){
            sendMovementToServer();
            lastUpdate -= updatePeriod;
        }*/
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
        manager.addListener(this, "forward", "backward", "strafeLeft", "strafeRight", "jump", "rotateleft", "rotateright");
    
    }
    
}
