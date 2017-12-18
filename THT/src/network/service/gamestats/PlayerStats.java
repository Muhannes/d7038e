/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import api.models.EntityType;
import control.TrapType;

/**
 *
 * @author hannes
 */
public class PlayerStats {
    private EntityType entityType; 
    private int freezeTraps = 3;
    private int slowTraps = 3;
    private int blindTraps = 3;

    public PlayerStats(EntityType entityType) {
        this.entityType = entityType;
    }
    
    private boolean decreaseFreezeTraps(){
        if (freezeTraps > 0) {
            freezeTraps--;
            return true;
        } else {
            return false;
        }
    }
    
    private boolean decreaseSlowTraps(){
        if (slowTraps > 0) {
            slowTraps--;
            return true;
        } else {
            return false;
        }
    }
    
    private boolean decreaseBlindTraps(){
        if (blindTraps > 0) {
            blindTraps--;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean decreaseTraps(TrapType type){
        switch(type){
            case Freeze:
                return decreaseFreezeTraps();
            case Slow:
                return decreaseSlowTraps();
            case Blind:
                return decreaseBlindTraps();
            default:
                return false;
        }
    }
}
