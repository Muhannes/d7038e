/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.models.Player;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;
import network.services.gamesetup.ClientGameSetupService;
import network.services.gamesetup.GameSetupSessionListener;
import network.services.gamesetup.PlayerInitEvent;
import network.services.gamesetup.StartGameEvent;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author ted
 */
public class SetupState extends BaseAppState implements EventListener{

    private SimpleApplication app;
    
    private ClientGameSetupService cgss;
    
    private int globalId;
    
    public SetupState(ClientGameSetupService cgss, int id){
        this.cgss = cgss;
        this.globalId = id;
    }
    
    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;    
        
    }

    @Override
    protected void cleanup(Application app) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEnable() {
        EventBus.subscribe(this);
        cgss.join(globalId);
        
        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), 
        app.getAudioRenderer(), app.getGuiViewPort());
                
        /** Create a new NiftyGUI object */
        Nifty nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gameState.xml", "game"); //Should be 2 screens, human and monster. 
        
        // attach the Nifty display to the gui view port as a processor
        app.getViewPort().addProcessor(niftyDisplay);
        
        
        buildStaticWorld();
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if(T == PlayerInitEvent.class){
            //INIT WORLD
            app.getRootNode();
            
            //Notify ready
            
        } else if (T == StartGameEvent.class){
            this.setEnabled(false);
            app.getStateManager().getState(GameState.class).setEnabled(true);            
        }
    }
    
    
    public void buildStaticWorld(){
        Box b = new Box(1,1,1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        app.getRootNode().attachChild(geom);
    }
}
