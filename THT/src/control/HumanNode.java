/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author hannes
 */
public class HumanNode extends EntityNode{

    public HumanNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name, position, bulletAppState, model);
    }

    @Override
    public void initEntity(Vector3f position) {
        this.setLocalTranslation(position);
        // Currently only works for Oto model.
        
        animationControl = model.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim("stand");
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent() / 2;
        float height = boundingBox.getYExtent() * 1.15f;
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);                
        charControl = new CharacterControl(shape, 1.0f); 
        this.addControl(charControl);
                
        //GhostControl used for collision
        GhostControl ghost = new GhostControl(new BoxCollisionShape(new Vector3f(0.1f,0.1f,0.1f)));
        this.addControl(ghost);
        this.getControl(GhostControl.class).setSpatial(model);
        
        bulletAppState.getPhysicsSpace().add(charControl);
        bulletAppState.getPhysicsSpace().add(ghost);
        
        
        attachChild(model);
    }

    @Override
    public void setWalkDirection(Vector3f walkDirection) {
        
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

    
}