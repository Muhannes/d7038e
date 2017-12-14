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
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import network.services.ping.PingSessionListener;

/**
 *
 * @author truls
 */
public class GameGUI implements ScreenController, PingSessionListener{

    private Screen screen;
    private Nifty nifty;
    private Element txtPing;
    
    public GameGUI(NiftyJmeDisplay display){
        nifty = display.getNifty();
        
        nifty.fromXml("Interface/game/game.xml", "game", this);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        this.screen = screen;
        txtPing = screen.findElementById("txtPing");
        
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
    
}
