/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.lobby;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class LobbyGUI implements ScreenController{
    
    private List<LobbyGUIListener> listeners;
    private NiftyJmeDisplay display;
    private Screen screen;
    private ListBox listBox;
    
    public LobbyGUI(NiftyJmeDisplay display){
        this.display = display;
        this.listeners = new ArrayList<>();
        
        /** Create a new NiftyGUI object */
        Nifty nifty = display.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/lobby.xml", "lobby", this);
        
        nifty.setDebugOptionPanelColors(true);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.screen = screen;
        listBox = screen.findNiftyControl("myListBox", ListBox.class);
    }

    @Override
    public void onStartScreen() {
        // Nothing
    }

    @Override
    public void onEndScreen() {
        // Nothing
    }
    
    @NiftyEventSubscriber(id="myListBox")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        List<String> selection = event.getSelection();
        selection.forEach(lobbyName -> 
                listeners.forEach(l -> l.onJoinLobby(lobbyName)));
    }
    
    /**
     * Add a lobby room to the list of lobbies in the gui
     * @param name Name of lobby
     */
    public void addLobbyRoom(String name){
        listBox.addItem(name);
    }
    
    /**
     * Remove all lobby rooms from the listbox 
     */
    public void clearLobbyRoomList(){
        listBox.clear();
    }
    
    public void addLobbyGUIListener(LobbyGUIListener lobbyGUIListener){
        listeners.add(lobbyGUIListener);
    }
    
    /**
     * Invoked when NewGame-button is pressed
     */
    public void newGame(){
        TextField field = screen.findNiftyControl("textfieldGamename", TextField.class);
        String gamename = field.getRealText();
        listeners.forEach(l -> l.onCreateLobby(gamename));
    }
    
    /**
     * Invoked when Quit-button is pressed
     */
    public void quitGame(){
        listeners.forEach(l -> l.onQuitGame());
    }
    
    /**
     * Invoked when refresh-button is pressed
     */
    public void refresh(){
        listeners.forEach(l -> l.onRefresh());
    }
    
}
