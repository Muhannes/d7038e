/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;  
import com.jme3.renderer.Camera;
import network.service.movement.client.ClientMovementService;

/**
 * This control handles how a human should react to keyboard input.
 * 
 * @author truls
 */
public class HumanInputControl extends AbstractInputControl{

    private BetterCharacterControl character;
    // Physical body that we use to control movment of spatial
    
    private Camera camera;
    // Camera chasing the player
    
    private Vector3f moveDirection;
    // Used to set walking direction
    
    private Vector3f camDir;
    // Used to set new movement direction
    
    private Vector3f camLeft;
    // Used to set new movement direction
    
    /*
    private Boolean forward = false;
    private Boolean backward = false;
    private Boolean left = false;
    private Boolean right = false;
    private Boolean strafeLeft = false;
    private Boolean strafeRight = false;*/

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
        
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(character == null){
            character = getSpatial().getControl(BetterCharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharactorControl to be attached to spatial");
            }
        }
        
        camDir = camera.getDirection().clone();
        camLeft = camera.getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        moveDirection.set(0, 0, 0);
        
        if(name.equals("forward")){
            moveDirection.addLocal(camDir);
        }
        
        if(name.equals("backward")){
            moveDirection.addLocal(camDir.negate());
        }
        
        if(name.equals("strafeLeft")){
            moveDirection.addLocal(camLeft);
        }
        
        if(name.equals("strafeRight")){
            moveDirection.addLocal(camLeft.negate());
        }
        
        character.setWalkDirection(moveDirection);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(character == null){
            character = getSpatial().getControl(BetterCharacterControl.class);
            if(character == null){
                throw new RuntimeException("HumanInputControl requires a BetterCharactorControl to be attached to spatial");
            }
        }
        /*
        if(name.equals("forward")) forward = isPressed;
        if(name.equals("backward")) backward = isPressed;
        if(name.equals("strafeLeft")) strafeLeft = isPressed;
        if(name.equals("strafeRight")) strafeRight = isPressed;
        if(name.equals("left")) left = isPressed;
        if(name.equals("right")) right = isPressed;*/
        
        if(name.equals("jump") && isPressed){
           character.jump();
        }
    }

}
