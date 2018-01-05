/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;

/**
 * This is class provides game background music that can easily be played
 * and stopped at any time.
 * @author truls
 */
public class AmbientAudioService {
    
    private static AmbientAudioService service;
    // Singleton
    
    private AudioNode gamebackground;
    // Ambient music played during the game
    
    private AudioNode pregamebackground;
    // Ambient music during the login/lobby phase
    
    private AmbientAudioService(AssetManager manager){
        gamebackground = new AudioNode(manager, "Sounds/Dark_Cue2.wav", AudioData.DataType.Stream);
        pregamebackground = new AudioNode(manager, "Sounds/Rachet.wav", AudioData.DataType.Stream);
        
        makeAmbient(gamebackground);
        makeAmbient(pregamebackground);
        
        pregamebackground.setVolume(0.1f);
    }
    
    public static AmbientAudioService getAmbientAudioService(AssetManager manager){
        if(service == null){
            service = new AmbientAudioService(manager);
        }
        
        return service;
    }
    
    public void playGameMusic(){
        gamebackground.play();
    }
    
    public void stopGameMusic(){
        gamebackground.stop();
    }
    
    public void playPreGameMusic(){
        pregamebackground.play();
    }
    
    public void stopPreGameMusic(){
        pregamebackground.stop();
    }
    
    private void makeAmbient(AudioNode node){
        node.setPositional(false);
        node.setDirectional(false);
        node.setLooping(true);
    }
    
}
