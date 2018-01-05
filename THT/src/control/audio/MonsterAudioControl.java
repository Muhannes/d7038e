/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.control.CharacterControl;

/**
 *
 * @author truls
 */
public class MonsterAudioControl extends AbstractAudioControl{

    private AudioNode footsteps;
    private CharacterControl character;
    
    public MonsterAudioControl(AssetManager manager){
        footsteps = new AudioNode(manager, "Sound/Effects/Foot steps.ogg", AudioData.DataType.Buffer);
        footsteps.setLooping(true);
        footsteps.setPositional(true);
        footsteps.setReverbEnabled(true);
        footsteps.setRefDistance(3); // Distance when soundlevel is half its original
        footsteps.setVolume(2.0f);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
       if(character == null){
           character = getSpatial().getControl(CharacterControl.class);
           if(character == null){
               throw new RuntimeException("MonsterAudioControl requires Charactercontrol");
           }
       }
       
       footsteps.setLocalTranslation(getSpatial().getLocalTranslation());
       //Update the audio nodes position
       
       footsteps.updateGeometricState(); 
       // Update state manually since this node is not attached to a parent
       
       boolean isWalking = character.getWalkDirection().lengthSquared() > 0;
       if( isWalking && footsteps.getStatus() != AudioSource.Status.Playing ){
           footsteps.play();
       }else if ( !isWalking ) {
           footsteps.stop();
       }
    }
    
}
