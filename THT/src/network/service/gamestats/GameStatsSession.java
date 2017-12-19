/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import com.jme3.math.Vector3f;
import control.trap.TrapType;

/**
 *
 * @author hannes
 */
public interface GameStatsSession {
    void layTrap(TrapType trap, Vector3f pos);
}
