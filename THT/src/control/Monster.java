/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Spatial;
import control.action.Jump;

/**
 *
 * @author ted
 */
public class Monster extends AbstractController implements ActionListener{

    private Jump jump;
    
    public Boolean forward = false, backward = false, left = false, right = false, strafeLeft = false, strafeRight = false;
    
    private final CharacterControl charController;
    private final Spatial self;
    private final AssetManager asset;
    private final SimpleApplication app;
    
    public final float movementSpeed = 3.0f;
   
    public Monster(Spatial player, SimpleApplication app){
        this.self = player;
        this.app = (SimpleApplication)app;
        this.asset = app.getAssetManager();
        this.charController = self.getControl(CharacterControl.class);
    
    }
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
    
        if(name.equals("jump")){ 
            charController.jump();
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
        if(name.equals("Decoy")){
        }       
    }

    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));   
        manager.addMapping("strafeLeft", new KeyTrigger(KeyInput.KEY_Q));
        manager.addMapping("strafeRight", new KeyTrigger(KeyInput.KEY_E));        
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        manager.addMapping("decoy", new KeyTrigger(KeyInput.KEY_F));
        
       manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "decoy");
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
