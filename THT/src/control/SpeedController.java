/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author hannes
 */
public class SpeedController extends AbstractControl {

    private float movementSpeed;
    private CharacterControl cc;

    public SpeedController(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
    
    
    @Override
    protected void controlUpdate(float tpf) {
        if (cc == null){
            cc = getSpatial().getControl(CharacterControl.class);
        } else {
            Vector3f newWalkDir = cc.getWalkDirection().normalize().mult(movementSpeed).mult(tpf);
            cc.setWalkDirection(newWalkDir);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
}
