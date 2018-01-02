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
import com.jme3.bullet.control.CharacterControl;
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
    
    public static float MOVEMENT_SPEED = 3.0f;
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
    
    public void convergeSnap(Vector3f position, Vector3f walkDirection, Quaternion rotation){
        charControl.setPhysicsLocation(position);
        setWalkDirection(walkDirection);
        if(walkDirection.length() > 0){
            Vector3f viewDirection = new Vector3f(walkDirection).normalize();
            viewDirection.y = 0;
            setViewDirection(viewDirection);
        }
    }
    
    
    public Vector3f getWalkDirection(){
        return charControl.getWalkDirection();
    }
    
    /**
     * Currently only works for "Oto" model.
     * @param walkDirection 
     */
    public abstract void setWalkDirection(Vector3f walkDirection);
    
    public void setViewDirection(Vector3f walkDirection){
        charControl.setViewDirection(walkDirection);
    }
    
    /**
     * Scales the movementSpeed depending on tpf
     * @param tpf 
     */
    public void scaleWalkDirection(float tpf){
        if(slowed){
            int tmpTimer = (int) ((System.currentTimeMillis() - timer)/1000);
            if(tmpTimer > slowTime){
                MOVEMENT_SPEED = NORMAL_MOVEMENT_SPEED;
                slowed = false;
            }            
        }
        Vector3f scaledSpeed = charControl.getWalkDirection().normalize().mult(MOVEMENT_SPEED).mult(tpf);
        setWalkDirection(scaledSpeed);
    }

    
    public void slowDown(){
        if(!slowed){
            LOGGER.log(Level.INFO, "Sloooowing down");
            timer = System.currentTimeMillis();
            slowed = true;
            MOVEMENT_SPEED = SLOWED_MOVEMENT_SPEED;
        }
    }
    
}    