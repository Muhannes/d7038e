/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.login.ClientLoginService;
import network.services.login.LoginSessionListener;

/**
 *
 * @author truls
 */
public class LoginScreen extends AbstractAppState implements 
        ScreenController,
        LoginSessionListener{    
    
    private static final Logger LOGGER = Logger.getLogger(LoginScreen.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    
    private LobbyScreen lobbyScreen;
    private ClientLoginService clientLoginService;
    
    public LoginScreen(ClientLoginService clientLoginService, LobbyScreen lobbyScreen){   
        this.lobbyScreen = lobbyScreen;
        this.clientLoginService = clientLoginService;
        this.clientLoginService.addLoginSessionListener(this);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);        
        this.app = app;
        
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/login.xml", "start", this); 
        
        // attach the Nifty display to the gui view port as a processor
        app.getViewPort().addProcessor(niftyDisplay);
    }
    
    @Override
    public void cleanup(){
        app.getViewPort().removeProcessor(niftyDisplay);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        // Nothing
    }

    @Override
    public void onStartScreen() {
        // Nothing
    }

    @Override
    public void onEndScreen() {
        // Nothing
    }

    public void startGame(){
        TextField field = nifty.getScreen("start").findNiftyControl("textfieldUsername", TextField.class);
        String username = field.getRealText();
        LOGGER.log(Level.INFO, "Username = {0}", new Object[]{username});
        if(username.length() != 0){
            clientLoginService.login(username);
        }
    }
    
    public void quitGame(){
        app.getStateManager().detach(this);
        app.stop();
    }

    @Override
    public void notifyLogin(boolean loggedIn) {
        if(loggedIn){
            app.getStateManager().detach(this);
            app.getStateManager().attach(lobbyScreen);
        }
    }
 
}
