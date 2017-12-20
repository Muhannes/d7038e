/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 *
 * @author ted
 */
public class PlayState extends BaseAppState{

    @Override
    protected void initialize(Application app) {
        
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        System.out.println("Playstate enabled!");
    }

    @Override
    protected void onDisable() {
        
    }
    
}
