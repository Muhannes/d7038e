/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
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
        float height = boundingBox.getYExtent();    
        
        model.setLocalTranslation(model.getLocalTranslation().add(0, height/2, 0));
        charControl = new BetterCharacterControl(radius, height, 1.0f);
        this.addControl(charControl);
                
        bulletAppState.getPhysicsSpace().add(charControl);
        
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
