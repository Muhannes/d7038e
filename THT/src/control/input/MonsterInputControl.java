/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import control.EntityNode;
import control.animation.MonsterAnimationControl;
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
       super.initKeys(manager);
       manager.addMapping("slash", new KeyTrigger(KeyInput.KEY_F));
       manager.addListener(this, "slash");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        super.onAction(name, isPressed, tpf);
        if (name.equals("slash") && isPressed) {
            getSpatial().getControl(MonsterAnimationControl.class).swordSlash();
        }
    }
    
}
