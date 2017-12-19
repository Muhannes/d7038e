/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.trap;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author hannes
 */
public class FreezeTrap extends Trap {
    private static final float FREEZE_TIME = 3.0f;

    public FreezeTrap(Vector3f pos) {
        super(pos, TrapType.Freeze);
    }

    /**
     * When a player collides with this trap, call this function.
     * @param character The player that stepped in the trap.
     */
    public static void applyEffect(BetterCharacterControl character) {
        // TODO: Make the player stand still for FREEZE_TIME
        // TODO: Optionally, make player "blueish" :)
    }
    
}
