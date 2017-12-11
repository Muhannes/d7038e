/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import gui.login.LoginGUI;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.login.ClientLoginService;
import network.services.login.LoginSessionListener;
import gui.login.LoginGUIListener;
import java.util.concurrent.Callable;

/**
 * 
 * @author truls
 */
public class LoginState extends BaseAppState implements 
        LoginSessionListener,
        LoginGUIListener{    
    
    private static final Logger LOGGER = Logger.getLogger(LoginState.class.getName());
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    
    private LobbyState lobbyScreen;
    private ClientLoginService clientLoginService;
    
    private String username;
    
    LoginGUI gui;
    
    public LoginState(ClientLoginService clientLoginService, LobbyState lobbyScreen){   
        this.lobbyScreen = lobbyScreen;
        this.clientLoginService = clientLoginService;
    }
    
    @Override
    public void initialize(Application app){  
        
        
        this.app = app;
        
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        
    }
    
    @Override
    public void cleanup(Application app){
    }

    @Override
    public void notifyLogin(boolean loggedIn) {
        if(loggedIn){
            lobbyScreen.setUsername(username);
            LoginState ls = this;
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    ls.setEnabled(false);
                    app.getStateManager().getState(LobbyState.class).setEnabled(true);
                }
            });
        }
    }

    @Override
    public void notifyLobbyServerInfo(String hostname, int port) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onStartGame(String username) {
        LOGGER.log(Level.INFO, "Logging in. Username: {0}", username);
        if(username.length() != 0){
            clientLoginService.login(username);
            this.username = username;
        }
    }

    @Override
    public void onQuitGame() {
        app.getStateManager().detach(this);
        app.stop();
    }

    @Override
    protected void onEnable() {
        gui = new LoginGUI(niftyDisplay);
        gui.addLoginScreeenListener(this);
        
        clientLoginService.addLoginSessionListener(this);  
        app.getViewPort().addProcessor(niftyDisplay);
    }

    @Override
    protected void onDisable() {
        app.enqueue(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
        gui.removeLoginScreeenListener(this);
        clientLoginService.removeLoginSessionListener(this);
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }
}
