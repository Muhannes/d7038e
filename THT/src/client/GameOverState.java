/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import gui.game.GameOverGUI;
import gui.game.GameGUIListener;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class GameOverState extends BaseAppState implements GameGUIListener{

    private static final Logger LOGGER = Logger.getLogger(GameOverState.class.getName());

    private NiftyJmeDisplay niftyDisplay;
    ClientApplication app;
    private GameOverGUI game;    
    
    @Override
    protected void initialize(Application app) {
        this.app = (ClientApplication) app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        
        game = new GameOverGUI(niftyDisplay);
    }

    @Override
    protected void cleanup(Application app) {
        niftyDisplay.getNifty().exit();
        niftyDisplay.cleanup();
        niftyDisplay = null;
    }

    @Override
    protected void onEnable() {
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        app.getInputManager().setCursorVisible(true);
        game.addLobbyGUIListener(this);
    }

    public void setWinner(String winners){ 
        game.endGame(winners);
    }
    
    @Override
    protected void onDisable() {     
        app.getGuiViewPort().removeProcessor(niftyDisplay);
        game.removeLobbyGUIListener(this);
    }

    @Override
    public void onQuit() {
        //release everything
        app.disconnectToGameServer();
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
