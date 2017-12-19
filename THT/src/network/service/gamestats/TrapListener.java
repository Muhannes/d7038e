/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

import control.trap.TrapType;

/**
 *
 * @author hannes
 */
public interface TrapListener {
    void notifyTrapEvent(int playerId, TrapType trap);
}
