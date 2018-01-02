/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.BulletAppState;
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
    
    public void slowDown(){
        if(!slowed){
            LOGGER.log(Level.INFO, "Sloooowing down");
            timer = System.currentTimeMillis();
            slowed = true;
            MOVEMENT_SPEED = SLOWED_MOVEMENT_SPEED;
        }
    }
    
}    