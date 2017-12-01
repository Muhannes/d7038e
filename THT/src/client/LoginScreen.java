/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author truls
 */
public class LoginScreen extends AbstractAppState implements ScreenController{    
    
    private static final Logger LOGGER = Logger.getLogger(LoginScreen.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    
    private LobbyScreen lobbyScreen;
    
    public LoginScreen(LobbyScreen lobbyScreen){   
        this.lobbyScreen = lobbyScreen;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.FINE, "Initializing LoginScreen");
        System.out.println("Init LoginScreen");
        super.initialize(stateManager, app);        
        this.app = app;
        
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/screen.xml", "start", this); 
        
        // attach the Nifty display to the gui view port as a processor
        app.getViewPort().addProcessor(niftyDisplay);
    }
    
    @Override
    public void cleanup(){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
        System.out.println("Cleanup loginscreen");
        app.getViewPort().removeProcessor(niftyDisplay);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("New Bind : " + nifty + " : " + screen);
        //TODO: 
    }

    @Override
    public void onStartScreen() {
        System.out.println("On start screen!");
    }

    @Override
    public void onEndScreen() {
        System.out.println("On end screen!");
        //app.getViewPort().removeProcessor(niftyDisplay);
    }

    public void startGame(String nextScreen){
        LOGGER.log(Level.FINE, "Start game");
        System.out.println("Nextscreen: " + nextScreen);
        app.getStateManager().detach(this);
        app.getStateManager().attach(lobbyScreen);
    }
    
    public void quitGame(){
        LOGGER.log(Level.FINE, "Quit game");
        app.getStateManager().detach(this);
        app.stop();
    }
    
}
