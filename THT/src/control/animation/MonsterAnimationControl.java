/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.animation;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.scene.Spatial;
import java.util.logging.Level;

/**
 *
 * @author Hannes
 */
public class MonsterAnimationControl extends AbstractAnimationControl implements AnimEventListener{
    private static String ATTACK_TYPE = "Attack3";
    private static String STAND = "Idle2";
    private static String WALK = "Walk";
    
    CharacterControl charControl;
    AnimControl animationControl;
    AnimChannel animationChannel;
    
    public MonsterAnimationControl(Spatial model){
        animationControl = model.getControl(AnimControl.class);
        // Prints out the different kinds of animations
        //for (String anim : animationControl.getAnimationNames()) {
        //    System.out.println(anim);
        //}
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim(STAND);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if(charControl == null){
            charControl = getSpatial().getControl(CharacterControl.class);
            if(charControl == null){
                throw new RuntimeException("MonsterAnimationControl requires CharacterControl");
            }
        }
        if(charControl.getWalkDirection().lengthSquared() > 0){
            walkAnimation();
        }else{
            standAnimation();
        }
    }
    
    private void walkAnimation(){
         if (!animationChannel.getAnimationName().equals(WALK) && !animationChannel.getAnimationName().equals(ATTACK_TYPE)) {
            animationChannel.setAnim(WALK, 1f);
        }
    }
    
    private void standAnimation(){
        if (!animationChannel.getAnimationName().equals(STAND) && !animationChannel.getAnimationName().equals(ATTACK_TYPE)) {
            animationChannel.setAnim(STAND);
        }
    }
    
    public void swordSlash(){
        System.out.println("Slash!");
        animationChannel.setAnim(ATTACK_TYPE, 1.0f);
        animationChannel.setLoopMode(LoopMode.DontLoop);
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals(ATTACK_TYPE)) {
            animationChannel.setAnim(STAND);
        }
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // Nothing
    }
}
