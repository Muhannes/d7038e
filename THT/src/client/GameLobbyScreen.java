/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
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
public class GameLobbyScreen extends BaseAppState implements 
        ChatSessionListener, 
        ClientLobbyListener,
        GameLobbyGUIListener{

    private static final Logger LOGGER = Logger.getLogger(GameLobbyScreen.class.getName());
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private String gameName;
    private ArrayList<String> players;
    LobbyState lobbyScreen;
    private ClientChatService ccs;
    private ClientLobbyService cls;
    
    private GameLobbyGUI gui;

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
        app.getAudioRenderer(), app.getGuiViewPort());
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
    
    public void setName(String name){
        this.gameName = name;
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
    public void allReady(String ip, int port) {
        LOGGER.fine("allReady method in GameLobbyScreen");
        // Connect to GameServer with (ip:port)!
        ((ClientApplication)app).connectToGameServer(ip, port);
        System.out.println("Done connecting");
        GameLobbyScreen gls = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                System.out.println("New runnable for enabling setupstate");
                gls.setEnabled(false);
                app.getStateManager().getState(SetupState.class).setEnabled(true);
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
        GameLobbyScreen gls = this;
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                gls.setEnabled(false);
                app.getStateManager().getState(LobbyState.class).setEnabled(true);
            }
        });
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

    @Override
    protected void onEnable() {
        
        gui = new GameLobbyGUI(niftyDisplay);
        
        gui.addGameLobbyGUIListener(this);
        
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        // attach the Nifty display to the gui view port as a processor
        ccs.addChatSessionListener(this);
        cls.addClientLobbyListener(this);
        
        for (String player : players) {
            playerJoinedLobby(player);
        }
    }

    @Override
    protected void onDisable() {
        players.clear();
        gui.clearChat();
        ccs.removeChatSessionListener(this);
        cls.removeClientLobbyListener(this);
        gui.removeGameLobbyGUIListener(this);
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }

}
