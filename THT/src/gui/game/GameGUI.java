/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.game;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import gui.event.EnterEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.ping.PingSessionListener;

/**
 *
 * @author truls
 */
public class GameGUI implements ScreenController, PingSessionListener, KeyInputHandler{

    private static final Logger LOGGER = Logger.getLogger(GameGUI.class.getName());

    private Screen screen;
    private Nifty nifty;
    private Element gameover;
    private Element quit;
    
    private final List<GameGUIListener> listeners;
    
    public GameGUI(NiftyJmeDisplay display){
        nifty = display.getNifty();
        this.listeners = new ArrayList<>();
        
        nifty.fromXml("Interface/game/game.xml", "game", this);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        LOGGER.log(Level.INFO, "bind?");
        this.screen = screen;
    }
    
    public void addLobbyGUIListener(GameGUIListener gameGUIListener){
        listeners.add(gameGUIListener);
    }
    
    public void removeLobbyGUIListener(GameGUIListener gameGUIListener){
        listeners.remove(gameGUIListener);
    }


    @Override
    public void onStartScreen() {
        // Nothing
    }

    @Override
    public void onEndScreen() {
        // Nothing
    }

    @Override
    public void notifyPing(int ms) {
//        txtPing.getRenderer(TextRenderer.class).setText("PING:" + ms);
    }
    
    @SuppressWarnings("null")
    public void endGame(String winners){
        LOGGER.log(Level.INFO, "The winners are : " + winners);
        try{            
            gameover = this.screen.findElementById("gameover");
            quit = this.screen.findElementById("quit");
            
        } catch(NullPointerException e){
            LOGGER.log(Level.SEVERE, e.toString());
        }

        if(winners.equals("humans")){
            gameover.getRenderer(TextRenderer.class).setText("Humans win!\nAll the monkeys have been found.");            
        }else{
            gameover.getRenderer(TextRenderer.class).setText("Monsters win!\nAll the silly humans are dead.");
        }
        quit.getRenderer(TextRenderer.class).setText("Game over!\nPress 'Enter' to return to lobby!");
    }

    @Override
    public boolean keyEvent(NiftyInputEvent nie) {
        if(nie instanceof EnterEvent){
            listeners.forEach(l -> l.onQuit());
            return true;
        }
        return false;
    }
}
