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
public class EntityNode extends Node implements AnimEventListener{
        private static final Logger LOGGER = Logger.getLogger(EntityNode.class.getName());

    // TODO: Init variables for different trap status, i.e. isFrozen.
    CharacterControl charControl;
    Spatial model;
    AnimControl animationControl;
    AnimChannel animationChannel;
    
    public static float MOVEMENT_SPEED = 3.0f;
    public static float SLOWED_MOVEMENT_SPEED = 1.0f;
    public static float NORMAL_MOVEMENT_SPEED = 3.0f;
    private long timer;
    private Boolean slowed = false;
    private final int slowTime = 3;
    
    public EntityNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name);
        initEntity(model, bulletAppState, position);
    }
    
    /**
     * Sets shape and looks of object. is currently a box, but can be changed to something else
     * @param material
     * @param bulletAppState
     * @param position 
     */
    private void initEntity(Spatial model, BulletAppState bulletAppState, Vector3f position){
        this.setLocalTranslation(position);
        // Currently only works for Oto model.
        animationControl = model.getControl(AnimControl.class);
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim("stand");
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        float width = boundingBox.getZExtent();
        //BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(radius, height, width));
        //charControl = new CharacterControl(boxShape, 1.0f);
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);                
        charControl = new CharacterControl(shape, 1.0f); 
        this.addControl(charControl);
                
        bulletAppState.getPhysicsSpace().add(charControl);
        
        attachChild(model);
    }
    
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
    public void setWalkDirection(Vector3f walkDirection){
        charControl.setWalkDirection(walkDirection);
        if (walkDirection.length() > 0) {
            if (!animationChannel.getAnimationName().equals("Walk")) {
                animationChannel.setAnim("Walk", 1f);
            }
        } else {
            if (!animationChannel.getAnimationName().equals("stand")) {
                animationChannel.setAnim("stand");
            }
        }
    }
    
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
            LOGGER.log(Level.INFO, ""+tmpTimer);
            if(tmpTimer > slowTime){
//                LOGGER.log(Level.INFO, "slow time over!");
                MOVEMENT_SPEED = NORMAL_MOVEMENT_SPEED;
                slowed = false;
            }            
        }
        Vector3f scaledSpeed = charControl.getWalkDirection().normalize().mult(MOVEMENT_SPEED).mult(tpf);
        setWalkDirection(scaledSpeed);
//        LOGGER.log(Level.INFO, "new tpf update");        
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
//        System.out.println("Cycle done!");
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
//        System.out.println("Animation changed");
    }
    
    public void slowDown(){
        if(!slowed){
            System.out.println("Sloooowing down");
            timer = System.currentTimeMillis();
            slowed = true;
            MOVEMENT_SPEED = SLOWED_MOVEMENT_SPEED;
        }
    }
    
}    