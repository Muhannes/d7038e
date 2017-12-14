/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.handover;

import java.util.Map;

/**
 *
 * @author hannes
 */
public interface HandoverSessionListener {
    
     void startSetup(Map<Integer, String> playerInfo);
}
