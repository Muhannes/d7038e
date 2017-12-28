/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 * The spatial for a movable character
 * @author hannes
 */
public class EntityNode extends Node{
    
    // TODO: Init variables for different trap status, i.e. isFrozen.
    CharacterControl charControl;
    Spatial model;
    
    public static final float MOVEMENT_SPEED = 3.0f;

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
        //charControl.setViewDirection(rotation); // maybe use this instead? but takes a vector3f...
        model.setLocalRotation(rotation);
        charControl.setWalkDirection(walkDirection);
    }
    
    public Vector3f getWalkDirection(){
        return charControl.getWalkDirection();
    }
    
    public void setWalkDirection(Vector3f walkDirection){
        charControl.setWalkDirection(walkDirection);
    }
    
    public void setViewDirection(Vector3f walkDirection){
        charControl.setViewDirection(walkDirection);
    }
    
    /**
     * Scales the movementSpeed depending on tpf
     * @param tpf 
     */
    public void scaleWalkDirection(float tpf){
        Vector3f scaledSpeed = charControl.getWalkDirection().normalize().mult(MOVEMENT_SPEED).mult(tpf);
        setWalkDirection(scaledSpeed);
    }
    
}

    