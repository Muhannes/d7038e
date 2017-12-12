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
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
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
public class GameLobbyScreen extends BaseAppState implements ScreenController, ChatSessionListener, ClientLobbyListener{

    private static final Logger LOGGER = Logger.getLogger(LoginState.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private Screen screen;
    private String gameName;
    private ArrayList<String> players;
    LobbyState lobbyScreen;
    private ClientChatService ccs;
    private ClientLobbyService cls;

    private final int GLOBAL_CHAT = ChatSpace.Chat.GLOBAL.ordinal();
    
    GameLobbyScreen(ClientChatService ccs, ClientLobbyService cls) {
        this.lobbyScreen = lobbyScreen;
        this.ccs = ccs;
        this.gameName = "No name";        
        this.players = new ArrayList<>();
        this.cls = cls;        
    }
    
    @Override
    public void initialize(Application app){
        LOGGER.log(Level.INFO, "Initializing LoginScreen");
        //super.initialize(stateManager, app);        
        this.app = app;
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
        
        
                      
        //nifty.setDebugOptionPanelColors(true);
                
    }

    @Override
    public void cleanup(Application app){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
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
    
    public void setName(String gameName){
        this.gameName = gameName;
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        //TODO: 
    }
    
    @Override
    public void onStartScreen() {
        LOGGER.fine("On start screen in GameLobbyScreen!");
        System.out.println("onStartScreen");
    }

    @Override
    public void onEndScreen() {
        LOGGER.fine("On end screen!");
    }

    public void startGame(String nextScreen){
        nifty.gotoScreen(nextScreen);
    }
    
    public void returnToLobby(){
        cls.leave();
        GameLobbyScreen gls = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                gls.setEnabled(false);
                app.getStateManager().getState(LobbyState.class).setEnabled(true);
            }
        });
    }
    
    public void quitGame(){
        cls.leave();
        app.getStateManager().detach(this);
        app.stop();
    }
    
    public void pressingReady(){        
        cls.ready();
        //Send message to chat that a player is ready
    }

    /**
     * A new message arrived.
     * @param message 
     * @param chat
     */
    @Override
    public void newMessage(String message, int chat) {
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBox", ListBox.class);
        field.addItem(message);
        
    }

    /**
     * Send chat message to server.
     * Server will delegate it to all receiptiants.
     */
    public void sendToServer(){
        LOGGER.fine("Sending message to server");
        TextField field = nifty.getScreen("gamelobby").findNiftyControl("textfieldInput", TextField.class);
        String chatInput = field.getRealText();
        if(chatInput != null){
            ccs.sendMessage(chatInput, GLOBAL_CHAT); // TOD0: Change destination of message
        }
        field.setText("");
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
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBoxPlayers", ListBox.class);
        field.addItem(name);
    }

    @Override
    public void playerLeftLobby(String name) {        
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBoxPlayers", ListBox.class);
        field.removeItem(name);
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
        GameLobbyScreen gls = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                gls.setEnabled(false);
                app.getStateManager().getState(SetupState.class).setEnabled(true);
            }
        });              
     }
    
    public void gameIsReady(){
        
    }

    @Override
    protected void onEnable() {
        
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gamelobby.xml", "gamelobby", this);
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);
        ccs.addChatSessionListener(this);
        cls.addClientLobbyListener(this);
        for (String player : players) {
            playerJoinedLobby(player);
        }
    }

    @Override
    protected void onDisable() {
        players.clear();
        
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBox", ListBox.class);
        field.clear();
        ccs.removeChatSessionListener(this);
        cls.removeClientLobbyListener(this);
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }

}
