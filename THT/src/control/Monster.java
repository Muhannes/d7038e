/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import control.action.Jump;

/**
 *
 * @author ted
 */
public class Monster extends AbstractController implements ActionListener{

    private Jump jump;
    
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("Jump")){
            jump.execute(entity);
        }
    }

    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));   
        manager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        manager.addMapping("Decoy", new KeyTrigger(KeyInput.KEY_F));
        
        manager.addListener(this, "Left", "Right", "Forward", "Backward", "Jump", "Decoy");
   }
    
        /*
    public void createMonster(){

        Texture demonSkin = asset.loadTexture("Models/demon/demon_tex.png");
        Material demonMat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        demonMat.setTexture("ColorMap", demonSkin);
        Spatial demon = worldRoot.getChild("demon");     
        demon.setMaterial(demonMat);
        bulletAppState.getPhysicsSpace().add(demon.getControl(RigidBodyControl.class));

    }
    */    

}
