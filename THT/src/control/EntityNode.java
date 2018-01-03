/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The spatial for a movable character
 * @author hannes
 */
public abstract class EntityNode extends Node{
        private static final Logger LOGGER = Logger.getLogger(EntityNode.class.getName());

    // TODO: Init variables for different trap status, i.e. isFrozen.
    CharacterControl charControl;
    Spatial model;
    AnimControl animationControl;
    AnimChannel animationChannel;
    
    BulletAppState bulletAppState;
    
    public float movementSpeed = NORMAL_MOVEMENT_SPEED;
    public static float SLOWED_MOVEMENT_SPEED = 1.0f;
    public static float NORMAL_MOVEMENT_SPEED = 3.0f;
    private long timer;
    private Boolean slowed = false;
    private final int slowTime = 3;
    
    public EntityNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name);
        this.model = model;
        this.bulletAppState = bulletAppState;
        initEntity(position);
    }
    
    /**
     * Sets shape and looks of object. is currently a box, but can be changed to something else
     * @param position 
     */
    public abstract void initEntity(Vector3f position);
    
    public void convergeLinear(Vector3f position, Vector3f rotation){
        // TODO: Set movementdirection pointing to that position
        
        //charControl.setWalkDirection(vectorPointingAtPosition);
    }
    
    public void convergeSnap(Vector3f position, Vector3f walkDirection, Vector3f rotation){
        charControl.warp(position);
        setWalkDirection(walkDirection);
        setViewDirection(rotation);
    }
    
    
    public Vector3f getWalkDirection(){
        return charControl.getWalkDirection();
    }
    
    public Vector3f getViewDirection(){
        return charControl.getViewDirection();
    }
    
    /**
     * Currently only works for "Oto" model.
     * @param walkDirection 
     */
    public abstract void setWalkDirection(Vector3f walkDirection);
    
    public void setViewDirection(Vector3f walkDirection){
        charControl.setViewDirection(walkDirection);
    }
    
    public void rotateY(float rotationRad){
        Vector3f oldRot = charControl.getViewDirection();
        float x = (FastMath.cos(rotationRad) * oldRot.x) + (FastMath.sin(rotationRad) * oldRot.z);
        float z = (FastMath.cos(rotationRad) * oldRot.z) - (FastMath.sin(rotationRad) * oldRot.x);
        charControl.setViewDirection(new Vector3f(x, oldRot.y, z));
    }
    
    
    /**
     * Scales the movementSpeed depending on tpf
     * @param tpf 
     */
    public void scaleWalkDirection(float tpf){
        Vector3f scaledSpeed = charControl.getWalkDirection().normalize().mult(movementSpeed).mult(tpf);
        setWalkDirection(scaledSpeed);
    }
    
    public void slowDown(){
        if(!slowed){
            LOGGER.log(Level.INFO, "Sloooowing down");
            timer = System.currentTimeMillis();
            slowed = true;
            movementSpeed = SLOWED_MOVEMENT_SPEED;
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
                    Vector3f wd = getWalkDirection();
                    if (wd.length() > 0) {
                        wd.normalizeLocal();
                        wd.multLocal(movementSpeed);
                    }
                }
            }).start();
            
            
        }
    }
    
}    