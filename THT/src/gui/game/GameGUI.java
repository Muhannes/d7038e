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
import gui.event.KeyBoardMapping;
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
        this.nifty = display.getNifty();
        this.listeners = new ArrayList<>();
        
        this.nifty.fromXml("Interface/game/gameover.xml", "gameover", this);
        LOGGER.log(Level.INFO, "Done loading" + this.nifty.getAllScreensName());
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        this.screen = screen;
        this.screen.addKeyboardInputHandler(new KeyBoardMapping(), this);
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
            gameover = this.screen.findElementById("winner");
            quit = this.screen.findElementById("quit");
            
        if(winners.equals("humans")){
            gameover.getRenderer(TextRenderer.class).setText("Humans win!\nAll the monkeys have been found."); 
        }else{
            gameover.getRenderer(TextRenderer.class).setText("Monsters win!\nAll the silly humans are dead.");
        }
        quit.getRenderer(TextRenderer.class).setText("Press 'Enter' to return to lobby!");
    }

    @Override
    public boolean keyEvent(NiftyInputEvent nie) {
/*        if(nie instanceof EnterEvent){
            LOGGER.log(Level.INFO, "Pressed Enter");
            listeners.forEach(l -> l.onQuit());
            return true;
        }
        return false;
*/
    return false;
    }
}
