/*
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
import gui.gamelobby.GameLobbyGUI;
import gui.gamelobby.GameLobbyGUIListener;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.ChatSessionListener;
import network.services.chat.ChatSpace;
import network.services.chat.ClientChatService;
import network.services.lobby.ClientLobbyListener;
import network.services.lobby.ClientLobbyService;

/**
 *
 * @author ted
 */
public class GameLobbyScreen extends AbstractAppState implements 
        ChatSessionListener, 
        ClientLobbyListener,
        GameLobbyGUIListener{

    private static final Logger LOGGER = Logger.getLogger(LoginState.class.getName());
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private String gameName;
    private ArrayList<String> players;
    LobbyState lobbyScreen;
    private ClientChatService ccs;
    private ClientLobbyService cls;
    
    private GameLobbyGUI gui;

    private final int GLOBAL_CHAT = ChatSpace.Chat.GLOBAL.ordinal();
    
    GameLobbyScreen(LobbyState lobbyScreen, ClientChatService ccs, ClientLobbyService cls, String gameName) {
        this.lobbyScreen = lobbyScreen;
        this.ccs = ccs;
        ccs.addChatSessionListener(this);
        this.gameName = gameName;        
        this.players = new ArrayList<>();
        this.cls = cls;        
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.INFO, "Initializing LoginScreen");
        super.initialize(stateManager, app);        
        this.app = app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        gui = new GameLobbyGUI(niftyDisplay);
        
        gui.addGameLobbyGUIListener(this);
        
        app.getGuiViewPort().addProcessor(niftyDisplay);
    }

    @Override
    public void cleanup(){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }

    public void addPlayers(String name){
        players.add(name);
    }
    
    public ArrayList<String> getPlayers(){
        return players;
    }
    
    public String getName(){
        return this.gameName;
    }

    /**
     * A new message arrived.
     * @param message 
     * @param chat
     */
    @Override
    public void newMessage(String message, int chat) {
        gui.addChatMessage(message);
    }

    /**
     * new player joined the room
     * @param name 
     * @param chat
     */
    @Override
    public void playerJoinedChat(String name, int chat) {
        //Display new player in chat.
        newMessage(name + " has joined the chat!", chat);
    }

    /**
     * player left the room
     * @param name 
     * @param chat
     */
    @Override
    public void playerLeftChat(String name, int chat) {
        //Player left from room.
        newMessage(name + " Left the chat!", chat);
    }

    @Override
    public void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers) {
        // Nothing
    }

    @Override
    public void playerJoinedLobby(String name) {
        gui.addPlayer(name);
    }

    @Override
    public void playerLeftLobby(String name) { 
        gui.removePlayer(name);
    }
    
    @Override
    public void playerReady(String name, boolean ready) {
        //TODO: display readyness
        newMessage(name + " is ready!", GLOBAL_CHAT);
    }
    
    @Override
    public void allReady() {
        // TODO: change to setupState
        LOGGER.fine("allReady method in GameLobbyScreen");
        app.getStateManager().detach(this);
        app.enqueue(new Callable(){
           @Override
           public Object call() throws Exception {
                app.getStateManager().getState(SetupState.class).setEnabled(true);   
                return true;
           } 
        });                
    }

    @Override
    public void onReady() {
        cls.ready();
    }

    @Override
    public void onReturnToLobby() {
        cls.leave();
        app.getStateManager().detach(this);
        app.getStateManager().attach(lobbyScreen);    
    }

    @Override
    public void onQuitGame() {
        cls.leave();
        app.getStateManager().detach(this);
        app.stop();
    }

    @Override
    public void onSendMessage(String message) {
      ccs.sendMessage(message, GLOBAL_CHAT); 
    }

}
