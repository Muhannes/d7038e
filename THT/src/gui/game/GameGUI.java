/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.game;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import gui.event.EnterEvent;
import java.util.ArrayList;
import java.util.List;
import network.service.ping.PingSessionListener;

/**
 *
 * @author truls
 */
public class GameGUI implements ScreenController, PingSessionListener, KeyInputHandler{

    private Screen screen;
    private Nifty nifty;
    private Element txtPing;
    private Element gameover;
    private Element quit;
    
    private List<GameGUIListener> listeners;
    
    public GameGUI(NiftyJmeDisplay display){
        nifty = display.getNifty();
        this.listeners = new ArrayList<>();
        
        nifty.fromXml("Interface/game/game.xml", "game", this);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        this.screen = screen;
        txtPing = screen.findElementById("txtPing");
        gameover = screen.findElementById("gameover");
        quit = screen.findElementById("quit");        
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
        txtPing.getRenderer(TextRenderer.class).setText("PING:" + ms);
    }
    
    public void endGame(String winners){
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
