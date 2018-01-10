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
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.swing.SwingUtilities2;

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
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        LOGGER.log(Level.SEVERE, "GameOverState enabled");
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        app.getInputManager().setCursorVisible(true);
        game = new GameOverGUI(niftyDisplay);
        game.addLobbyGUIListener(this);
    }

    public void setWinner(String winners){ 
        game.endGame(winners);
    }
    
    @Override
    protected void onDisable() {     
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
        game.removeLobbyGUIListener(this);
        niftyDisplay.cleanup();
        niftyDisplay = null;
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
