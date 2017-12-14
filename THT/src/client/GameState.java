/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import de.lessvoid.nifty.Nifty;

/**
 *
 * @author ted
 */
public class GameState extends BaseAppState {
    
    private Application app;
    private NiftyJmeDisplay niftyDisplay;
    private Nifty nifty;
    
    
    private boolean left = false, right = false, forward = false, backward = false;
    private final Vector3f walkingDirection = Vector3f.ZERO;
    private ChaseCamera chaseCamera;
    private Camera camera;
    
    
    @Override
    protected void initialize(Application app) {
        this.app = app;

        nifty = niftyDisplay.getNifty();

        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/gamelobby.xml", "frame");
        
        // attach the Nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        //Set ChaseCamera to player with (camera,player,inputmanager)
        
    }

    @Override
    protected void cleanup(Application app) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEnable() {
        System.out.println("I'M IN GAMESTATE NOW");
    }

    @Override
    protected void onDisable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void update(float tpf){
        /*        
        
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();

        camDir.y = 0;
        camLeft.y = 0;
        
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        walkingDirection.set(0,0,0);
        
        if(left) walkingDirection.addLocal(camLeft);
        if(right) walkingDirection.addLocal(camLeft.negate());
        if(forward) walkingDirection.addLocal(camDir);
        if(backward) walkingDirection.addLocal(camDir.negate());
        
        if(avatar != null){ 
            walkingDirection.multLocal(40f).multLocal(tpf);
            playerEntity.getController().setWalkDirection(walkingDirection);
        } */
    }
    
}
