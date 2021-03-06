/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.animation;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.scene.Spatial;

/**
 *
 * @author Hannes
 */
public class HumanAnimationControl extends AbstractAnimationControl{
    
    private CharacterControl charControl;
    private AnimControl animationControl;
    private AnimChannel animationChannel;
    
    public HumanAnimationControl(Spatial model){
        animationControl = model.getControl(AnimControl.class);
        // Prints out the different kinds of animations
        //for (String anim : animationControl.getAnimationNames()) {
        //    System.out.println(anim);
        //}
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim("stand");
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if(charControl == null){
            charControl = getSpatial().getControl(CharacterControl.class);
            if(charControl == null){
                throw new RuntimeException("HumanAnimationControl requires CharacterControl");
            }
        }
        
        if(charControl.getWalkDirection().lengthSquared() > 0){
            walkAnimation();
        }else{
            standAnimation();
        }
    }
    
    private void walkAnimation(){
        if (!animationChannel.getAnimationName().equals("Walk")) {
            animationChannel.setAnim("Walk", 1f);
        }
    }
    
    private void standAnimation(){
        if (!animationChannel.getAnimationName().equals("stand")) {
            animationChannel.setAnim("stand");
        }
    }
}
