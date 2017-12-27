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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.chat.ChatSessionListener;
import network.service.chat.client.ClientChatService;
import network.service.lobby.LobbyRoom;
import network.service.lobby.client.ClientLobbyService;
import network.service.lobby.LobbySessionListener;

/**
 *
 * @author ted
 */
public class GameLobbyState extends BaseAppState implements 
        ChatSessionListener, 
        LobbySessionListener,
        GameLobbyGUIListener{

    private static final Logger LOGGER = Logger.getLogger(GameLobbyState.class.getName());
    
    private NiftyJmeDisplay niftyDisplay;
    private ClientApplication app;
    private String gameName;
    private ArrayList<String> players;
    LobbyState lobbyScreen;
    private ClientChatService chatService;
    private ClientLobbyService lobbyService;
    private int roomID;
    
    private GameLobbyGUI gui;
    
    GameLobbyState() {
        this.gameName = "No name";        
        this.players = new ArrayList<>();
    }
    
    @Override
    public void initialize(Application app){        
        this.app = (ClientApplication) app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
    }

    @Override
    public void cleanup(Application app){
        
    }
    
    @Override
    protected void onEnable() {
        try {
            chatService = app.getClientChatService();
            chatService.addChatSessionListener(this);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Chat service is offline");
        }
        lobbyService = app.getClientLobbyService();
        gui = new GameLobbyGUI(niftyDisplay);
        
        gui.addGameLobbyGUIListener(this);
        
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        lobbyService.addClientLobbyListener(this);
        
        for (String player : players) {
            playerJoinedLobby(player);
        }
    }

    @Override
    protected void onDisable() {
        players.clear();
        gui.clearChat();
        if (chatService != null) {
            chatService.removeChatSessionListener(this);
        }
        lobbyService.removeClientLobbyListener(this);
        gui.removeGameLobbyGUIListener(this);
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }

    public void addPlayers(String name){
        players.add(name);
    }
    
    public ArrayList<String> getPlayers(){
        return players;
    }
    
    public void setID(int id){
        this.roomID = id;
    }
    
    public int getID(){
        return roomID;
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
        app.enqueue(() -> {
            gui.addChatMessage(message);
        });
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
        newMessage(name + " Left the chat!", chat);
    }

    @Override
    public void updateLobby(List<LobbyRoom> room) {
        // Nothing
    }

    @Override
    public void playerJoinedLobby(String name) {
        app.enqueue(() -> {
            gui.addPlayer(name);
        });
    }

    @Override
    public void playerLeftLobby(String name) { 
        app.enqueue(() -> {
            gui.removePlayer(name);
        });
    }
    
    @Override
    public void playerReady(String name, boolean ready) {
        app.enqueue(() -> {
            if(ready){
                gui.addChatMessage(name + " is ready!");
            }else{
                gui.addChatMessage(name + "is not ready");
            }
        });
    }
    
    @Override
    public void allReady(String ip, int port) {
        LOGGER.log(Level.INFO, "Connecting to game server at {0}:{1}", new Object[]{ip, port});
        ((ClientApplication)app).connectToGameServer(ip, port);
        app.enqueue(() -> {
            this.setEnabled(false);
            app.getStateManager().getState(SetupState.class).setEnabled(true);
        });
    }

    @Override
    public void onReady() {
        lobbyService.ready();
    }

    @Override
    public void onReturnToLobby() {
        lobbyService.leave();
        if (chatService != null) {
            chatService.leavechat(roomID);
        }
        
        GameLobbyState gls = this;
        app.enqueue(() -> {
            gls.setEnabled(false);
            app.getStateManager().getState(LobbyState.class).setEnabled(true);
        });
    }

    @Override
    public void onQuitGame() {
        lobbyService.leave();
        app.getStateManager().detach(this);
        app.stop();
    }

    @Override
    public void onSendMessage(String message) {
        if (chatService != null) {
            chatService.sendMessage(message, roomID);
        }
    }

    @Override
    public void joinedLobby(LobbyRoom room) {
        // Nothing
    }

}
