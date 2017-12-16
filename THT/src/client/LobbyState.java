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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.ClientChatService;
import network.services.lobby.ClientLobbyListener;
import network.services.lobby.ClientLobbyService;
import network.services.login.Account;
import network.services.login.ClientLoginService;

/**
 *
 * @author ted
 */
public class LobbyState extends BaseAppState implements 
        LobbyGUIListener,
        ClientLobbyListener{

    private static final Logger LOGGER = Logger.getLogger(LobbyState.class.getName());
    
    private NiftyJmeDisplay niftyDisplay;
    private ClientApplication app;
    private Map<String, Integer> rooms;
    
    private ClientChatService clientChatService;
    private ClientLobbyService clientLobbyService;
    private GameLobbyScreen gameLobbyScreen;
    
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
    
    /**
     * Enter a GameLobbyScreen
     * @param gls 
     */
    public void joinGame(GameLobbyScreen gls){
        LOGGER.log(Level.FINE, "Joining game {0}", gls.getName());
        LobbyState ls = this;
        if (clientChatService != null) {
            clientChatService.joinchat(gls.getID());
        }
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                ls.setEnabled(false);
                gls.setEnabled(true);
            }
        });
    }

    /**
     * Updates the list of lobbyRooms available to choose
     * TODO: Deleteion of rooms are not supported yet.
     * @param lobbyName
     * @param roomID
     * @param numPlayers
     * @param maxPlayers 
     */
    @Override
    public void updateLobby(String lobbyName, int roomID, int numPlayers, int maxPlayers) {
        LOGGER.log(Level.INFO, "Lobby room udpated. Name: {0}, id: {1}, Players: {2}, "
                + "Max-players: {3}", new Object[]{lobbyName, roomID, numPlayers, maxPlayers});
        if(rooms.containsKey(lobbyName)){
            return;
        }
        rooms.put(lobbyName, roomID);
        gui.addLobbyRoom(lobbyName);
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
        int roomID = rooms.get(lobbyName);
        List<String> playerNames = clientLobbyService.join(roomID);
        if (playerNames != null) {
            LOGGER.log(Level.INFO, "Number of players in room: {0}", playerNames.size());
            GameLobbyScreen gls = app.getStateManager().getState(GameLobbyScreen.class);
            gls.setName(lobbyName);
            gls.setID(roomID);
            playerNames.forEach(name -> gls.addPlayers(name));
            joinGame(gls);
        }
    }

    @Override
    public void onRefresh() {
        rooms = clientLobbyService.getAllRooms();
        gui.clearLobbyRoomList();
        rooms.forEach((name, id) -> gui.addLobbyRoom(name));
    }

    @Override
    public void onCreateLobby(String lobbyName) {
        if(!lobbyName.isEmpty()){
            int roomID = clientLobbyService.createLobby(lobbyName);
            if (roomID != -1) {
                GameLobbyScreen gls = app.getStateManager().getState(GameLobbyScreen.class);
                gls.addPlayers(ClientLoginService.getAccount().name);
                gls.setName(lobbyName);
                gls.setID(roomID);
                joinGame(gls);
            } else {
                LOGGER.log(Level.INFO, "Server did not approve new name.");
            }
        } else {
            //TODO: Let server check name and return false if name is ""
            LOGGER.log(Level.INFO, "Must have a name!");
        }
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
        gui = new LobbyGUI(niftyDisplay);
        clientLobbyService.addClientLobbyListener(this);

        app.getGuiViewPort().addProcessor(niftyDisplay);

        // Fetching available lobbyRooms
        rooms = clientLobbyService.getAllRooms();
        rooms.forEach((name, id) -> gui.addLobbyRoom(name));
        gui.addLobbyGUIListener(this);
    }

    @Override
    protected void onDisable() {
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
        gui.removeLobbyGUIListener(this);
        clientLobbyService.removeClientLobbyListener(this);
    }
  
}

