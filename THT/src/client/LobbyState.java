/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import gui.lobby.LobbyGUI;
import gui.lobby.LobbyGUIListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.chat.client.ClientChatService;
import network.service.lobby.LobbyRoom;
import network.service.lobby.client.ClientLobbyService;
import network.service.login.Account;
import network.service.login.client.ClientLoginService;
import network.service.lobby.LobbySessionListener;

/**
 *
 * @author ted
 */
public class LobbyState extends BaseAppState implements 
        LobbyGUIListener,
        LobbySessionListener{

    private static final Logger LOGGER = Logger.getLogger(LobbyState.class.getName());
    
    private NiftyJmeDisplay niftyDisplay;
    private ClientApplication app;
    private Map<String, Integer> rooms;
    
    private ClientChatService clientChatService;
    private ClientLobbyService clientLobbyService;
    private GameLobbyState gameLobbyScreen;
    
    private LobbyGUI gui;
    
    private boolean lobbyAuthenticated = false;
    private boolean chatAuthenticated = false;

    public LobbyState(){
    }        
    
    @Override
    public void initialize(Application app){
        LOGGER.log(Level.FINE, "Initializing LoginScreen"); 
        
        this.app = (ClientApplication) app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
            app.getAssetManager(), app.getInputManager(), 
            app.getAudioRenderer(), app.getGuiViewPort()
        );
    }    

    @Override
    public void cleanup(Application app){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
    }
        
    @Override
    protected void onEnable() {
        clientLobbyService = app.getClientLobbyService();
        // Create GUI
        if (!lobbyAuthenticated) {
            Account acc = ClientLoginService.getAccount();
            clientLobbyService.authenticate(acc.id, acc.key);
            lobbyAuthenticated = true;
        }
        if (!chatAuthenticated) {
            Account acc = ClientLoginService.getAccount();
            try {
                clientChatService = app.getClientChatService();
                clientChatService.authenticate(acc.id, acc.key, acc.name);
                chatAuthenticated = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        rooms = new HashMap<>();
        
        gui = new LobbyGUI(niftyDisplay);
        app.getGuiViewPort().addProcessor(niftyDisplay);
        gui.addLobbyGUIListener(this);
        
        clientLobbyService.addClientLobbyListener(this);
        clientLobbyService.fetchAllRooms();
    }

    @Override
    protected void onDisable() {
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
        gui.removeLobbyGUIListener(this);
        clientLobbyService.removeClientLobbyListener(this);
    }
    
    /**
     * Enter a GameLobbyScreen
     * @param gls 
     */
    public void joinGame(GameLobbyState gls){
        LOGGER.log(Level.FINE, "Joining game {0}", gls.getName());
        LobbyState ls = this;
        if (clientChatService != null) {
            clientChatService.joinchat(gls.getID());
        }
        app.enqueue(() -> {
            ls.setEnabled(false);
            gls.setEnabled(true);
        });
    }

    @Override
    public void updateLobby(List<LobbyRoom> room) {
        room.forEach(r -> {
            LOGGER.log(Level.INFO, "Lobby room udpated. Name: {0}, Players: {2}, "
                    + "Max-players: {3}", new Object[]{r.getName(), r.numberOfPlayers(), r.maxPlayers()});
            app.enqueue(() -> {
                if(rooms.containsKey(r.getName())){
                    return;
                }
                rooms.put(r.getName(), 0);
                gui.addLobbyRoom(r.getName());
            });
        });
    }

    @Override
    public void playerJoinedLobby(String name) {
        // DO nothing
    }

    @Override
    public void playerLeftLobby(String name) {
        // DO nothing
    }

    @Override
    public void playerReady(String name, boolean ready) {
        // DO nothing
    }
    
    @Override
    public void allReady(String ip, int port) {
        // DO nothing
    }
    
    public Map<String, Integer> getGames(){
        return rooms;
    }   

    @Override
    public void onQuitGame() {
        LOGGER.log(Level.INFO, "Quitting system!");
        app.getStateManager().detach(this);
        app.stop();
    }

    @Override
    public void onJoinLobby(String lobbyName) {
        clientLobbyService.join(lobbyName);
    }

    @Override
    public void onRefresh() {
        gui.clearLobbyRoomList();
        clientLobbyService.fetchAllRooms();
    }

    @Override
    public void onCreateLobby(String lobbyName) {
        clientLobbyService.join(lobbyName);
    }

    @Override
    public void joinedLobby(LobbyRoom room) {
        GameLobbyState gls = app.getStateManager().getState(GameLobbyState.class);
        gls.setName(ClientLoginService.getAccount().name);
        gls.setID(room.getChatId());
        room.getPlayers().forEach(name -> gls.addPlayers(name));
        joinGame(gls);
    }
  
}

