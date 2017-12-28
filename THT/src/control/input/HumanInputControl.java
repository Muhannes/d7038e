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

/**
 * This control handles how a human should react to keyboard input.
 * @author truls
 */
public class HumanInputControl extends AbstractInputControl{

    private BetterCharacterControl character;
    
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
        
        Vector3f viewDir = character.getViewDirection().clone();
        
        if(name.equals("forward")){
            character.setWalkDirection(viewDir.mult(tpf));
        }
        if(name.equals("backward")){
            character.setWalkDirection(viewDir.negate().mult(tpf));
        }
    }

}
