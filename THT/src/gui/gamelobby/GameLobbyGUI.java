/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.gamelobby;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class GameLobbyGUI implements ScreenController{
    
    private List<GameLobbyGUIListener> listeners;
    
    private NiftyJmeDisplay display;
    
    private Screen screen;
    
    public GameLobbyGUI(NiftyJmeDisplay display){
        this.display = display;
        this.listeners = new ArrayList<>();
        
        /** Create a new NiftyGUI object */
        Nifty nifty = display.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gamelobby.xml", "gamelobby", this);
        
        //nifty.setDebugOptionPanelColors(true);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
        //Nothing
    }

    @Override
    public void onEndScreen() {
        //Nothing
    }
    
    public void addChatMessage(String message){
        ListBox field = screen.findNiftyControl("myListBox", ListBox.class);
        field.addItem(message);
    }
    
    public void addPlayer(String name){
        ListBox field = screen.findNiftyControl("myListBoxPlayers", ListBox.class);
        field.addItem(name);
    }
    
    public void removePlayer(String name){
        ListBox field = screen.findNiftyControl("myListBoxPlayers", ListBox.class);
        field.removeItem(name);
    }
    
    public void ready(){
        listeners.forEach(l -> l.onReady());
    }
    
    public void returnToLobby(){
        listeners.forEach(l -> l.onReturnToLobby());
    }
    
    public void quitGame(){
        listeners.forEach(l -> l.onQuitGame());
    }
    
    public void sendMessage(){
        TextField field = screen.findNiftyControl("textfieldInput", TextField.class);
        String message = field.getRealText();
        listeners.forEach(l -> l.onSendMessage(message));
        field.setText("");
    }
    
    public void addGameLobbyGUIListener(GameLobbyGUIListener gameLobbyGUIListener){
        listeners.add(gameLobbyGUIListener);
    }
}
