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
import control.action.Jump;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 *
 * @author ted
 */
public class Human extends AbstractController implements ActionListener{

    private Jump jump;
    
    public Boolean forward = false, backward = false, left = false, right = false;
    
    private final CharacterControl charController;
    private final Spatial self;
    private final AssetManager asset;
    private final SimpleApplication app;
    
    private final float movementSpeed = 3.0f;
    
    public Human(Spatial player, SimpleApplication app){
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
        if(name.equals("trap")){
            if(isPressed){
                createTrap();
            }
        }           
    }

    /**
     * TODO: Use the new Trap objects
     */
    public void createTrap(){
        Box box = new Box(0.1f,0.1f,0.1f);
        Geometry geom = new Geometry("Box", box);
        Material material = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(material);
        geom.setLocalTranslation(self.getLocalTranslation());        
        app.getRootNode().attachChild(geom);
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
