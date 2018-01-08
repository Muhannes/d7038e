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
import de.lessvoid.nifty.tools.Color;
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
        
        nifty.fromXml("Interface/game/game.xml", "game");
        LOGGER.log(Level.INFO, "Done loading");
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        LOGGER.log(Level.INFO, "bind?\n" + screen + " \n" + nifty);
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
    }
    
    public void endGame(String winners){
        LOGGER.log(Level.INFO, "The winners are : " + winners);
/*                   
            gameover = this.screen.findElementById("gameover");
            quit = this.screen.findElementById("quit");
            
        if(winners.equals("humans")){
            LOGGER.log(Level.INFO, "humans");
            gameover.getRenderer(TextRenderer.class).setText("Humans win!\nAll the monkeys have been found."); 
            gameover.getRenderer(TextRenderer.class).setColor(Color.WHITE);
        }else{
            LOGGER.log(Level.INFO, "monsters");
            gameover.getRenderer(TextRenderer.class).setText("Monsters win!\nAll the silly humans are dead.");
            gameover.getRenderer(TextRenderer.class).setColor(Color.WHITE);
        }
        LOGGER.log(Level.INFO, "set quit test");
        quit.getRenderer(TextRenderer.class).setText("Press 'Enter' to return to lobby!");
        quit.getRenderer(TextRenderer.class).setColor(Color.WHITE);
        */
    }

    @Override
    public boolean keyEvent(NiftyInputEvent nie) {
        if(nie instanceof EnterEvent){
            LOGGER.log(Level.INFO, "Pressed Enter");
            listeners.forEach(l -> l.onQuit());
            return true;
        }
        return false;
    }
}
