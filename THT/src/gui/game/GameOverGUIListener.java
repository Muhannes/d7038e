/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.game;

import com.jme3.network.service.rmi.Asynchronous;

/**
 *
 * @author ted
 */
public interface GameOverGUIListener {
    
    @Asynchronous
    void onQuit();
}
