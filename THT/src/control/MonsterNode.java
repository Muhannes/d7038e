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
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import control.animation.MonsterAnimationControl;
import control.input.MonsterInputControl;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class MonsterNode extends EntityNode{
    
    private static final Logger LOGGER = Logger.getLogger(MonsterNode.class.getName());

    
    public MonsterNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name, position, bulletAppState, model);
    }
    
    @Override
    public void initEntity(Vector3f position) {
        this.setLocalTranslation(position);
        // Currently only works for Oto model.
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        model.rotate(0, FastMath.DEG_TO_RAD * 180, 0);
        model.setLocalTranslation(model.getLocalTranslation().add(0, - height*3/4, 0));
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);  
        charControl = new CharacterControl(shape, 1.0f);
        this.addControl(charControl);
                
        bulletAppState.getPhysicsSpace().add(charControl);
        
        this.addControl(new MonsterAnimationControl(model));
        
        // Speed scaling
        this.addControl(new SpeedController());
        
        attachChild(model);
    }
    
    public MonsterAnimationControl getAnimation(){
        return this.getControl(MonsterAnimationControl.class);
    }
    
    public MonsterInputControl getInput(){
        return this.getControl(MonsterInputControl.class);
    }
}
