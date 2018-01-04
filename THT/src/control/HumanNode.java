/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import control.animation.HumanAnimationControl;

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
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent() / 2;
        float height = boundingBox.getYExtent();    
        model.setLocalTranslation(model.getLocalTranslation().add(0, height/2, 0));
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);  
        charControl = new CharacterControl(shape, height);        
        this.addControl(charControl);
        
        bulletAppState.getPhysicsSpace().add(charControl);
                        
        this.addControl(new HumanAnimationControl(model));
                
        attachChild(model);
    }

}
