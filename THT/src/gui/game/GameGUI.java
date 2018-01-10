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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ted
 */
public class GameGUI implements ScreenController{

    private static final Logger LOGGER = Logger.getLogger(GameGUI.class.getName());

    private Screen screen;
    private Nifty nifty;
    private Element text1;
    private Element text2;
    private Element text3;
    private NiftyJmeDisplay display;
    
    public GameGUI(NiftyJmeDisplay display){
        this.display = display;
        this.nifty = display.getNifty();
        
        this.nifty.fromXml("Interface/game/gamegui.xml", "gamegui", this);
//        nifty.setDebugOptionPanelColors(true);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {  
        this.screen = screen;
    }
    

    public void displayKiller(){
        text1 = this.screen.findElementById("death");
        text1.getRenderer(TextRenderer.class).setText("A human got slain");     
        text1.startEffect(EffectEventId.onCustom, new FadeInEnd1(), "fadeIn1");
    }
    
    public void displayCatch(){
        text2 = this.screen.findElementById("caught");        
        text2.getRenderer(TextRenderer.class).setText("A monkey got caught"); 
        text2.startEffect(EffectEventId.onCustom, new FadeInEnd2(), "fadeIn2");
    }
    
    public void displayTraps(int traps){
        text3 = this.screen.findElementById("traps");        
        text3.getRenderer(TextRenderer.class).setText("You have " + traps + " traps!"); 
    }
    
    public void displayTooltip(){
        text3 = this.screen.findElementById("traps");        
        text3.getRenderer(TextRenderer.class).setText("Use 'f' to use traps!");         
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
        // Nothing
        display.cleanup();   
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
