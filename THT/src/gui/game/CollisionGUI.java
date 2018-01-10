/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.game;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import gui.event.KeyBoardMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ted
 */
public class CollisionGUI implements ScreenController{

    private static final Logger LOGGER = Logger.getLogger(CollisionGUI.class.getName());

    private Screen screen;
    private Nifty nifty;
    private Element text1;
    private Element text2;
    
    public CollisionGUI(NiftyJmeDisplay display){
        this.nifty = display.getNifty();
        
        this.nifty.fromXml("Interface/game/gamegui.xml", "gamegui", this);
//        LOGGER.log(Level.INFO, "Done loading" + this.nifty.getAllScreensName());
//        nifty.setDebugOptionPanelColors(true);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        this.screen = screen;
    }
    

    public void displayKiller(){
//        LOGGER.log(Level.INFO, "display killer");
        text1 = this.screen.findElementById("death");
        text1.getRenderer(TextRenderer.class).setText("A human got slain");     
        text1.startEffect(EffectEventId.onCustom, new FadeInEnd1(), "fadeIn1");
    }
    
    public void displayCatch(){
//        LOGGER.log(Level.INFO, "display catch");
        text2 = this.screen.findElementById("caught");        
        text2.getRenderer(TextRenderer.class).setText("A monkey got caught"); 
        text2.startEffect(EffectEventId.onCustom, new FadeInEnd2(), "fadeIn2");
    }
    
    @Override
    public void onStartScreen() {
        // Nothing
    }

    @Override
    public void onEndScreen() {
        // Nothing
    }
        
    class FadeInEnd1 implements EndNotify{

        @Override
        public void perform() {
            text1.startEffect(EffectEventId.onCustom, new FadeOutEnd1(), "fadeOut1");
        }
    }
    
    class FadeOutEnd1 implements EndNotify{

        @Override
        public void perform() {
            text1.getRenderer(TextRenderer.class).setText("");
        }        
    }
    
    class FadeInEnd2 implements EndNotify{

        @Override
        public void perform() {
            text2.startEffect(EffectEventId.onCustom, new FadeOutEnd2(), "fadeOut2");
        }        
    }
    
    class FadeOutEnd2 implements EndNotify{

        @Override
        public void perform() {
            text2.getRenderer(TextRenderer.class).setText("");
        }        
    }
}
