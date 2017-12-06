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
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.text.Text;
import network.services.chat.ChatSessionListener;
import network.services.chat.ClientChatService;

/**
 *
 * @author ted
 */
public class GameLobbyScreen extends AbstractAppState implements ScreenController, ChatSessionListener{

    private static final Logger LOGGER = Logger.getLogger(LoginScreen.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private Screen screen;
    private String gameName;
    private ArrayList<String> players;
    LobbyScreen lobbyScreen;
    private Chat chat;
    private ClientChatService ccs;
    
    GameLobbyScreen(LobbyScreen lobbyScreen, ClientChatService ccs, String gameName) {
        this.lobbyScreen = lobbyScreen;
        this.ccs = ccs;
        ccs.addChatSessionListener(this);
        this.gameName = gameName;        
        this.players = new ArrayList<>();
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.INFO, "Initializing LoginScreen");
        super.initialize(stateManager, app);        
        this.app = app;
        
        this.niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
        
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gamelobby.xml", "gamelobby", this);
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);
                      
        //nifty.setDebugOptionPanelColors(true);
        
        for(String name : players){
            playerJoined(name);
        }
    }

    @Override
    public void cleanup(){
        LOGGER.log(Level.FINE, "Cleanup LoginScreen");
        app.getViewPort().removeProcessor(niftyDisplay);
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
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        //TODO: 
    }
    
    @Override
    public void onStartScreen() {
        System.out.println("On start screen in GameLobbyScreen!");
    }

    @Override
    public void onEndScreen() {
        System.out.println("On end screen!");
    }

    public void startGame(String nextScreen){
        nifty.gotoScreen(nextScreen);
    }
    
    public void returnToLobby(){
        System.out.println("Returning to lobby!");
        
        app.getStateManager().detach(this);
        app.getStateManager().attach(lobbyScreen);
    }
    
    public void quitGame(){
        System.out.println("Stopping!");
//        playerLeft();
        app.getStateManager().detach(this);
        ccs.stop();
        app.stop();
    }

    @Override
    public void newMessage(String message) {
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBox", ListBox.class);
        field.addItem(message);
    }

    public void sendToServer(){
        System.out.println("Sending to server");
        TextField field = nifty.getScreen("gamelobby").findNiftyControl("textfieldInput", TextField.class);
        String chatInput = field.getRealText();
        if(chatInput != null){
            System.out.println("New Input : " + chatInput);
            ccs.sendMessage(chatInput);
        }
        field.setText("");
    }
    
    @Override
    public void playerJoined(String name) {
        //Display new player in chat.
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBoxPlayers", ListBox.class);
        field.addItem(name);        
    }

    @Override
    public void playerLeft(String name) {
        //Player left from room.
        System.out.println("Leaving room!");
        ListBox field = nifty.getScreen("gamelobby").findNiftyControl("myListBoxPlayers", ListBox.class);
        field.removeItem(name);        
    }
    
}
