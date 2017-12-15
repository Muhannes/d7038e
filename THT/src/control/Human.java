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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
 *
 * @author ted
 */
public class Human extends AbstractController implements ActionListener{

    private Jump jump;
    
    public Boolean forward = false, backward = false, left = false, right = false;
    
    private final CharacterControl charController;
    private final Spatial self;
    
    private final float movementSpeed = 3.0f;
    
    public Human(Spatial player){
        this.self = player;
        this.charController = self.getControl(CharacterControl.class);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        System.out.println("New action");
        if(name.equals("jump")){ 
            charController.jump();
        }
        if(name.equals("forward")){
            if(isPressed){
                charController.setWalkDirection(new Vector3f(-movementSpeed*tpf, 0f, 0f));            
            }else{
                charController.setWalkDirection(new Vector3f(0f, 0f, 0f));                   
            }
        }
        if(name.equals("backward")){
            if(isPressed){
                charController.setWalkDirection(new Vector3f(movementSpeed*tpf, 0f, 0f));
            }else{
                charController.setWalkDirection(new Vector3f(0f, 0f, 0f));            
            }
        }
        if(name.equals("left")){
            if(isPressed){
                charController.setViewDirection(new Vector3f(0f, 0f, movementSpeed*tpf));
                charController.setWalkDirection(new Vector3f(0f, 0f, movementSpeed*tpf));
            }else{
                charController.setWalkDirection(new Vector3f(0f, 0f, 0f));            
            }
        }
        if(name.equals("right")){
            if(isPressed){
                charController.setViewDirection(new Vector3f(0f, 0f, -movementSpeed*tpf));
                charController.setWalkDirection(new Vector3f(0f, 0f, -movementSpeed*tpf));
            }else{
                charController.setWalkDirection(new Vector3f(0f, 0f, 0f));            
            }
        
        }
            
    }

    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        manager.addMapping("trap", new KeyTrigger(KeyInput.KEY_F));        
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        manager.addListener(this, "left", "right", "forward", "backward", "jump", "trap");
    }
    
}
