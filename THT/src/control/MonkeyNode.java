/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author hannes
 */
public class MonkeyNode extends EntityNode {

    private static final float MONKEY_MOVEMENT_SPEED = 5;
    public MonkeyNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name, position, bulletAppState, model);
    }

    @Override
    public void initEntity(Vector3f position) {
        this.setLocalTranslation(position);
        // Currently only works for Oto model.
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent() / 4; 
        
        float height = boundingBox.getYExtent() / 2;
        model.setLocalTranslation(model.getLocalTranslation().subtract(0, height, 0));
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);
        charControl = new CharacterControl(shape, 1.0f);
        charControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        charControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        this.addControl(charControl);
        bulletAppState.getPhysicsSpace().add(charControl);
        
        // Speed scaling
        this.addControl(new SpeedController(MONKEY_MOVEMENT_SPEED));
        
        attachChild(model);
    }
    
}
