/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;  
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.client.ClientMovementService;
import control.EntityNode;
import java.util.logging.Logger;
/**
 * This control handles how a human should react to keyboard input.
 * 
 * @author truls
 * @author ted
 * @author hannes
 */
public class HumanInputControl extends AbstractInputControl{
    
    private static final Logger LOGGER = Logger.getLogger(HumanInputControl.class.getName());
    
    private EntityNode self;
    private int traps = 5;
    
    public HumanInputControl(EntityNode self, ClientMovementService movementService, ClientGameStatsService gameStatsService) {
        super(movementService, gameStatsService); 
        this.self = self;
    }
    
    @Override
    public void initKeys(InputManager manager) {
        super.initKeys(manager);
        manager.addMapping("trap", new KeyTrigger(KeyInput.KEY_F)); 
        manager.addListener(this, "trap");
    }
    
    public int getTrapRemaining(){
        return traps;
    }
    
    @Override
    public void disableKeys(InputManager manager){
        super.disableKeys(manager);
        manager.deleteMapping("trap");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        super.onAction(name, isPressed, tpf);
        
        if(name.equals("trap") && isPressed){            
            if(traps > 0){
                sendTrapsToServer();
                traps--;                
            }
        }
    }
    
    private void sendTrapsToServer(){
        Vector3f position = self.getLocalTranslation();
        self.setLocalTranslation(position);
        String trapName = self.getName() + ":" + traps;
        gameStatsService.notifyTrapPlaced(trapName, position);
    }    
}
