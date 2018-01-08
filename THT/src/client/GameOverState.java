/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 *
 * @author hannes
 */
public class GameOverState extends BaseAppState {

    ClientApplication app;
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        backToLobby();
    }

    @Override
    protected void onDisable() {
        
    }
    
    private void backToLobby(){
        GameOverState gos = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                gos.setEnabled(false);
                app.getStateManager().getState(LobbyState.class).setEnabled(true);
            }
        });
    }
    
}
