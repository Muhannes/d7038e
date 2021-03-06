/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import control.animation.MonsterAnimationControl;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.client.ClientMovementService;
import control.EntityNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This control handles how a monster should react to keyboard input.
 * 
 * @author truls
 * @author ted
 * @author hannes
 */
public class MonsterInputControl extends AbstractInputControl{

    private static final Logger LOGGER = Logger.getLogger(MonsterInputControl.class.getName());
    
    private EntityNode self; //Will be used for decoy 

    public MonsterInputControl(EntityNode self, ClientMovementService movementService, ClientGameStatsService gameStatsService) {
        super(movementService, gameStatsService);
        this.self = self;
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
            if(getSpatial() == null){
                LOGGER.log(Level.SEVERE, "spatial is null");
            }
            if(getSpatial().getControl(MonsterAnimationControl.class) == null){
                LOGGER.log(Level.SEVERE, "monster control is null");
            }
            getSpatial().getControl(MonsterAnimationControl.class).swordSlash();
            gameStatsService.notifySlash(getSpatial().getName());
        }
    }
    
    @Override
    public void disableKeys(InputManager input){
        super.disableKeys(input);
        input.deleteMapping("slash");
    }
}
