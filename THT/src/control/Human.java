/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import control.action.Jump;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 *
 * @author ted
 */
public class Human extends AbstractController implements ActionListener{

    private Jump jump;
    private CharacterControl control;
    
    public Boolean forward = false, backward = false, left = false, right = false;
    
    public Human(){
        jump = new Jump();        
        control = new CharacterControl();
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        System.out.println("New action");
        if(name.equals("Jump")){ 
            System.out.println("Jump?");
            jump.execute(entity);
        } else if(name.equals("forward")){
            forward = true;
        } else if(name.equals("backward")){
            backward = true;
        } else if(name.equals("left")){
            left = true;
        } else if(name.equals("right")){
            right = true;
        } else {}
    }

    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        manager.addMapping("Trap", new KeyTrigger(KeyInput.KEY_F));        
        manager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        manager.addListener(this, "Left", "Right", "Forward", "Backward", "Jump", "Trap");
    }
    
    
    public CharacterControl getController(){
        System.out.println("Controller : " + control);
        return control;
    }
    
}
