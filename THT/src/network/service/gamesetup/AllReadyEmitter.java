/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamesetup;

/**
 *
 * @author ted
 */
public interface AllReadyEmitter {
    
    void addListener(AllReadyListener listener);
    
}
