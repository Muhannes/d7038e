/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

import com.jme3.network.service.rmi.Asynchronous;
import java.util.Map;

/**
 *
 * @author hannes
 */
public interface HandoverSessionListener {
    
    @Asynchronous
    void startSetup(Map<Integer, String> playerInfo);
}
