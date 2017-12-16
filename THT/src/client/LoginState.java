/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import gui.login.LoginGUI;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.login.client.ClientLoginService;
import network.service.login.LoginSessionListener;
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
    private ClientApplication app;
    
    private ClientLoginService clientLoginService;
    
    private String username;
    
    LoginGUI gui;
    
    public LoginState(){
    }
    
    @Override
    public void initialize(Application app){  
        this.app = (ClientApplication) app;
        
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        
    }
    
    @Override
    public void cleanup(Application app){
    }

    /**
     * A login attempt has been approved or denied.
     * TODO: Do something with key (password)
     * @param loggedIn
     * @param key 
     */
    @Override
    public void notifyLogin(boolean loggedIn, String key, int id, String name) {
        if(loggedIn){
            //((ClientApplication)app).connectToLobbyServer();
            LobbyState lobbyState = app.getStateManager().getState(LobbyState.class);
            LoginState ls = this;
            app.enqueue(() -> {
                ls.setEnabled(false);
                lobbyState.setEnabled(true);
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
        clientLoginService = app.getClientLoginService();
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
