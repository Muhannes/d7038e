/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The spatial for a movable character
 * @author hannes
 */
public abstract class EntityNode extends Node{
    private static final Logger LOGGER = Logger.getLogger(EntityNode.class.getName());
    // TODO: Init variables for different trap status, i.e. isFrozen.
    MyCharacterControl charControl;
    Spatial model;
    Spatial lowDetailModel;
    AnimControl animationControl;
    AnimChannel animationChannel;
    
    BulletAppState bulletAppState;
    AssetManager assetManager;
    
    public float movementSpeed = NORMAL_MOVEMENT_SPEED;
    public static float SLOWED_MOVEMENT_SPEED = 1.0f/60.0f;
    public static float NORMAL_MOVEMENT_SPEED = 3.0f/60.0f;
    private long timer;
    private Boolean slowed = false;
    private final int slowTime = 3;
    
    public EntityNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model, AssetManager assetManager) {
        super(name);
        this.model = model;
        this.bulletAppState = bulletAppState;
        this.assetManager = assetManager;
        initEntity(position);
    }
    
    public void createLowDetailModel(AssetManager assetManager, ColorRGBA c, float width, float height){
        // Low detail level model
        Box box = new Box(width, height, width);
        Geometry geom = new Geometry("lowdetail", box);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", c);
        geom.setMaterial(material);
        lowDetailModel = geom;
        
    }
        
    /**
     * Sets up model and controllers that defines the behaviour of this entity
     * @param position 
     */
    public abstract void initEntity(Vector3f position);

    public Spatial getmodel(){
        return model;
    }
    
    public void changeModel(Vector3f pos, int distance){
        if (pos.distance(this.getWorldTranslation()) > distance) {
            if (this.getChild(lowDetailModel.getName()) == null){
                this.detachChild(model);
                this.attachChild(lowDetailModel);
            }
        } else {
            if (this.getChild(model.getName()) == null){
                this.detachChild(lowDetailModel);
                this.attachChild(model);
            }
        }
    }
    
    public void jumped(){
        charControl.jump();
    }
        
    public void slowDown(){
        if(!slowed){
            timer = System.currentTimeMillis();
            slowed = true;
            movementSpeed = SLOWED_MOVEMENT_SPEED;
            charControl.setMovementSpeed(movementSpeed);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(EntityNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    slowed = false;
                    movementSpeed = NORMAL_MOVEMENT_SPEED;
                    charControl.setMovementSpeed(movementSpeed);
                    /*Vector3f wd = charControl.getWalkDirection();
                    if (wd.length() > 0) {
                        wd.normalizeLocal();
                        wd.multLocal(movementSpeed);
                    }*/
                }
            }).start();            
        }
    }
    
}    