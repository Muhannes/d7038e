/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import gui.game.GameGUI;
import gui.game.GameGUIListener;

/**
 *
 * @author hannes
 */
public class GameOverState extends BaseAppState implements GameGUIListener{

    ClientApplication app;
    private GameGUI game;
    
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        game.addLobbyGUIListener(this);
    }

    public void setWinner(String winners){
        game.endGame(winners);
    }
    
    @Override
    protected void onDisable() {        
    }

    @Override
    public void onQuit() {
        //release everything
        game.removeLobbyGUIListener(this);
        backToLobby();
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
