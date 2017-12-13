/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.login;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import gui.event.EnterEvent;
import gui.event.KeyBoardMapping;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truls
 */
public class LoginGUI implements ScreenController, KeyInputHandler{
    
    private List<LoginGUIListener> listeners = new ArrayList<>();
    
    private Nifty nifty;
    
    private Screen screen;
    
    public LoginGUI(NiftyJmeDisplay display){
    
        /** Create a new NiftyGUI object */
        nifty = display.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/login.xml", "start", this); 
        
    }
    
    public void addLoginScreeenListener(LoginGUIListener loginGUIListener){
        listeners.add(loginGUIListener);
    }
    
    public void removeLoginScreeenListener(LoginGUIListener loginGUIListener){
        listeners.remove(loginGUIListener);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.screen = screen;
        this.screen.addKeyboardInputHandler(new KeyBoardMapping(), this);
    }

    @Override
    public void onStartScreen() {
        // Nothing
    }

    @Override
    public void onEndScreen() {
        // Nothing
    }
    
    public void startGame(){
        TextField field = screen.findNiftyControl("textfieldUsername", TextField.class);
        String username = field.getRealText();
        listeners.forEach(l -> l.onStartGame(username));
    }

    public void quitGame(){
        listeners.forEach(l -> l.onQuitGame());
    }

    @Override
    public boolean keyEvent(NiftyInputEvent nie) {
        if(nie instanceof EnterEvent){
            startGame();
            return true;
        }
        return false;
    }
}
