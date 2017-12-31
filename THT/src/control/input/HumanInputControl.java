/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;  
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 * This control handles how a human should react to keyboard input.
 * 
 * @author truls
 */
public class HumanInputControl extends AbstractInputControl{

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

    public HumanInputControl(ClientMovementService movementService, Camera camera) {
        super(movementService);
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
        
        manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "trap");
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        moveDirection.normalize();
        if(character != null) {
            character.setWalkDirection(moveDirection);
            character.setViewDirection(moveDirection);
            
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
                throw new RuntimeException("HumanInputControl requires a CharacterControl to be attached to spatial");
            }
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
                throw new RuntimeException("HumanInputControl requires a CharacterControl to be attached to spatial");
            }
        }
        
        if(name.equals("jump") && isPressed){
           character.jump();
        }
    }
    
    /**
     * Sends information about model to server
     */
    private void sendMovementToServer(){         
        Spatial self = getSpatial();
        PlayerMovement pm = new PlayerMovement(self.getName(), self.getLocalTranslation(),
                character.getWalkDirection(), self.getLocalRotation());
        movementService.sendMessage(pm);
    }

}
