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
import gui.lobby.LobbyGUI;
import gui.lobby.LobbyGUIListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.ClientChatService;
import network.services.lobby.ClientLobbyListener;
import network.services.lobby.ClientLobbyService;

/**
 *
 * @author ted
 */
public class LobbyState extends AbstractAppState implements 
        LobbyGUIListener,
        ClientLobbyListener{

    private static final Logger LOGGER = Logger.getLogger(LobbyState.class.getName());
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private Map<String, Integer> rooms;
    
    private final ClientLobbyService clientLobbyService;
    private GameLobbyScreen gameLobbyScreen;

    private ClientChatService clientChatService;
    
    private LobbyGUI gui;
    
    private String username = null;

    public LobbyState(ClientChatService ccs, ClientLobbyService cls){
        this.clientChatService = ccs;
        this.clientLobbyService = cls;
    }        
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.FINE, "Initializing LoginScreen");
        super.initialize(stateManager, app);   
        
        clientLobbyService.addClientLobbyListener(this);
        this.app = app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        gui = new LobbyGUI(niftyDisplay);
        gui.addLobbyGUIListener(this);
        
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        // Fetching available lobbyRooms
        rooms = clientLobbyService.getAllRooms();
        
        rooms.forEach((name, id) -> gui.addLobbyRoom(name));
    }    

    @Override
    public void cleanup(){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
        app.getViewPort().removeProcessor(niftyDisplay);
        niftyDisplay.getNifty().exit();
    }
    
    /**
     * Enter a GameLobbyScreen
     * @param gls 
     */
    public void joinGame(GameLobbyScreen gls){
        clientLobbyService.addClientLobbyListener(gls);
        LOGGER.log(Level.FINE, "Joining game {0}", gls.getName());
        app.getStateManager().detach(this);
        app.getStateManager().attach(gls);
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
    public void allReady() {
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
        List<String> playerNames = clientLobbyService.join(rooms.get(lobbyName));
        if (playerNames != null) {
            GameLobbyScreen gls = new GameLobbyScreen(this, clientChatService, clientLobbyService, lobbyName);
            LOGGER.log(Level.INFO, "Number of players in room: {0}", playerNames.size());
            playerNames.forEach(name -> gls.addPlayers(name));
            joinGame(gls);
        }
    }

    @Override
    public void onRefresh() {
        rooms = clientLobbyService.getAllRooms();
        rooms.forEach((name, id) -> gui.addLobbyRoom(name));
    }

    @Override
    public void onCreateLobby(String lobbyName) {
        if(!lobbyName.isEmpty()){
            boolean created = clientLobbyService.createLobby(lobbyName);
            if (created) {
                GameLobbyScreen gls = new GameLobbyScreen(this, clientChatService, clientLobbyService, lobbyName);
                gls.addPlayers("me"); //TODO: use real name for me
                joinGame(gls);
            } else {
                LOGGER.log(Level.INFO, "Server did not approve new name.");
            }
        } else {
            //TODO: Let server check name and return false if name is ""
            LOGGER.log(Level.INFO, "Must have a name!");
        }
    }
    
    public void setUsername(String username){
        this.username = username;
    }
  
}

