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
import com.sun.xml.internal.ws.util.StringUtils;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.ChatTextSendEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.Scrollbar;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.chatcontrol.ChatEntryModelClass;
import de.lessvoid.nifty.controls.chatcontrol.builder.ChatBuilder;
import de.lessvoid.nifty.controls.listbox.ListBoxItemProcessor;
import de.lessvoid.nifty.controls.listbox.builder.ListBoxBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ted
 */
public class LobbyScreen extends AbstractAppState implements ScreenController{

    private static final Logger LOGGER = Logger.getLogger(LobbyScreen.class.getName());
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private Application app;
    private Screen screen;
    private ListBox listBox;
    private ArrayList<GameLobbyScreen> games;
    private Chat chat;
    
    private GameLobbyScreen gameLobbyScreen;
       
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        LOGGER.log(Level.FINE, "Initializing LoginScreen");
        System.out.println("Init LobbyScreen");
        super.initialize(stateManager, app);        
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
        games = new ArrayList();

    }    

    @NiftyEventSubscriber(id="myListBox")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        List<String> selection = event.getSelection();
        for(String selectedItem : selection) {
            System.out.println("listbox selection [ " + selectedItem + " ] \nThe index in games is : " + games.get(selection.indexOf(selectedItem)).getName());
            joinGame(games.get(selection.indexOf(selectedItem)));
        }
    }
    
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
    
    public void newGame(){
        TextField field = nifty.getScreen("lobby").findNiftyControl("textfieldGamename", TextField.class);
        String gamename = field.getRealText();
        if(!gamename.isEmpty()){
            GameLobbyScreen tmp = new GameLobbyScreen(this, gamename);
            System.out.println("Trying to create new map!");
            if(games.add(tmp)){
                System.out.println("Created the game : " + tmp.getName());
                listBox.addItem(tmp.getName());   
            }
            System.out.println("Games size : " + games.size());
        } else {
            System.out.println("Failed to created, no name.");
            LOGGER.log(Level.FINE, "Must have a name!");
        }
    }
    
    public void quitGame(){
        System.out.println("Stopping!");
        app.getStateManager().detach(this);
        app.stop();
    }
        
}

