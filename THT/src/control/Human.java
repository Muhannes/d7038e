/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

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
    
    public Human(){
        jump = new Jump();
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        System.out.println("New action");
        if(name.equals("Jump")){ 
            System.out.println("Jump?");
            jump.execute(entity);
        }
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
    
}
