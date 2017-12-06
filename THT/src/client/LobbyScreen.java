/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.LobbyRoom;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.HostedConnection;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.ClientChatService;
import network.services.lobby.ClientLobbyListener;
import network.services.lobby.ClientLobbyService;
import network.util.ConnectionAttribute;

/**
 *
 * @author ted
 */
public class LobbyScreen extends AbstractAppState implements ScreenController, ClientLobbyListener{

    private static final Logger LOGGER = Logger.getLogger(LobbyScreen.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private Screen screen;
    private ListBox listBox;
    private Map<String, Integer> games;
    private Chat chat;
    private final ClientLobbyService clientLobbyService;
    
    private GameLobbyScreen gameLobbyScreen;
    private ClientChatService ccs;

    public LobbyScreen(ClientChatService ccs, ClientLobbyService cls){
        this.ccs = ccs;
        this.clientLobbyService = cls;
    }        
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.FINE, "Initializing LoginScreen");
        System.out.println("Init LobbyScreen");
        super.initialize(stateManager, app);   
        
        clientLobbyService.addClientLobbyListener(this);
        this.app = app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/lobby.xml", "lobby", this);
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);

        //List of games
        listBox = screen.findNiftyControl("myListBox", ListBox.class);
        // Fetching available lobbyRooms
        games = clientLobbyService.getAllRooms();
        // Adding them to list
        listBox.addAllItems(games.keySet());
    }    

    /**
     * When choosing an already existing lobbyRoom
     * @param id
     * @param event 
     */
    @NiftyEventSubscriber(id="myListBox")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        List<String> selection = event.getSelection();
        for(String gameName : selection) {
            System.out.println("listbox selection [ " + gameName + " ] \nThe game id is : " + games.get(gameName));
            LobbyRoom lobbyRoom = clientLobbyService.join(games.get(gameName));
            if (lobbyRoom != null) {
                GameLobbyScreen gls = new GameLobbyScreen(this, ccs, gameName);
                for (HostedConnection player : lobbyRoom.getPlayers()) {
                    gls.addPlayers(player.getAttribute(ConnectionAttribute.NAME));
                    System.out.println("" + player.getAttribute(ConnectionAttribute.NAME));
                }
                joinGame(gls);
            }
        }
    }
    
    /**
     * Enter a GameLobbyScreen
     * @param gls 
     */
    public void joinGame(GameLobbyScreen gls){
        System.out.println("Joining game.");
        app.getStateManager().detach(this);
        app.getStateManager().attach(gls);
        LOGGER.log(Level.FINE, "Wait until the game has loaded.");
    }
    
    @Override
    public void cleanup(){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
        app.getViewPort().removeProcessor(niftyDisplay);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.screen = screen;
        this.nifty = nifty;
        //TODO: 
    }

    @Override
    public void onStartScreen() {
        System.out.println("On start screen in LobbyScreen!");
    }

    @Override
    public void onEndScreen() {
        System.out.println("On end screen!");
    }

    public void startGame(){
        System.out.println("Starting");
    //    nifty.gotoScreen(nextScreen);
    }
    
    /**
     * When creating a new game.
     * Request server for a new lobbyroom,
     * if ok, join it.
     */
    public void newGame(){
        TextField field = nifty.getScreen("lobby").findNiftyControl("textfieldGamename", TextField.class);
        String gamename = field.getRealText();
        if(!gamename.isEmpty()){
            LobbyRoom lobbyRoom = clientLobbyService.createLobby(gamename);
            if (lobbyRoom != null) {
                GameLobbyScreen gls = new GameLobbyScreen(this, ccs, gamename);
                gls.addPlayers("me"); //TODO: use real name for me
                joinGame(gls);
            } else {
                System.out.println("Failed to create, server did not approve.");
                LOGGER.log(Level.FINE, "Server did not approve new name.");
            }
        } else {
            LOGGER.log(Level.FINE, "Must have a name!");
        }
    }
    
    public void quitGame(){
        LOGGER.log(Level.FINE, "Quitting system!");
        app.getStateManager().detach(this);
        app.stop();
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
        System.out.println("Update Lobby Received!");
        for (Object object : listBox.getItems()) {
            String name = (String) object;
            if (name == lobbyName) {
                return;
            }

        }
        games.put(lobbyName, roomID);
        listBox.addItem(lobbyName);
    }

    @Override
    public void playerJoined(String name) {
        // DO nothing
    }

    @Override
    public void playerLeft(String name) {
        // DO nothing
    }

    @Override
    public void playerReady(String name, boolean ready) {
        // DO nothing
    }
        
}

