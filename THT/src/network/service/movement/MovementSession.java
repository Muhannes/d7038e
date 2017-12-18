/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement;

import com.jme3.math.Vector3f;
import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author ted
 */
public interface MovementSession {
    
    @Asynchronous
    void sendMessage(Vector3f location, int id);
    
    @Asynchronous
    void joinedMovement(int id);
    
    @Asynchronous
    void leftMovement(int id);

}
