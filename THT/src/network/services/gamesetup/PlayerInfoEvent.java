/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamesetup;

import java.util.Map;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class PlayerInfoEvent extends Event {
    public Map<Integer, String> playerInfo;
    public PlayerInfoEvent(Map<Integer, String> playerInfo) {
        this.playerInfo = playerInfo;
    }
    
}
