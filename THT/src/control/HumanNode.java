/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import control.animation.HumanAnimationControl;
import control.input.HumanInputControl;

/**
 *
 * @author hannes
 */
public class HumanNode extends EntityNode{
    
    public HumanNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model, AssetManager assetManager) {
        super(name, position, bulletAppState, model, assetManager);
    }

    @Override
    public void initEntity(Vector3f position) {
        this.setLocalTranslation(position);
        // Currently only works for Oto model.
        
        BoundingBox boundingBox = (BoundingBox) model.getWorldBound();
        
        float radius = boundingBox.getXExtent() / 2; 
        
        float height = boundingBox.getYExtent() * 1.15f;
        createLowDetailModel(assetManager, ColorRGBA.Black, radius, height);
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);
        charControl = new MyCharacterControl(shape, 1.0f);
        charControl.setMovementSpeed(movementSpeed);
        this.addControl(charControl);
        
        bulletAppState.getPhysicsSpace().add(charControl);
                        
        this.addControl(new HumanAnimationControl(model));
        
        // Speed scaling
        //this.addControl(new SpeedController());
        
        attachChild(model);
    }

    public HumanInputControl getInput(){
        return this.getControl(HumanInputControl.class);
    }
    
    public HumanAnimationControl getAnimation(){
        return this.getControl(HumanAnimationControl.class);
    }
    
}
