/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import control.EntityNode;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.client.ClientMovementService;

/**
 * This control handles how a monster should react to keyboard input.
 * 
 * @author truls
 * @author ted
 * @author hannes
 */
public class MonsterInputControl extends AbstractInputControl{

    public MonsterInputControl(EntityNode player, ClientMovementService movementService, ClientGameStatsService gameStatsService) {
        super(movementService, gameStatsService);
    }

    @Override
    public void initKeys(InputManager manager) {
       manager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));   
        manager.addMapping("strafeLeft", new KeyTrigger(KeyInput.KEY_Q));
        manager.addMapping("strafeRight", new KeyTrigger(KeyInput.KEY_E));        
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        manager.addMapping("decoy", new KeyTrigger(KeyInput.KEY_F));
        
        manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "decoy");
    }

    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
       
    }
    
}
