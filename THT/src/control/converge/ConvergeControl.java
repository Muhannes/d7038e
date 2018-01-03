/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.converge;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.List;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 * This control will converge a Spatial to a given Setpoint. This control, once
 * added as listener to a MovementSession, will receive new setpoints continously 
 * from a server and try to converge the controller spatial to this point
 * 
 * @author truls
 */
public class ConvergeControl extends AbstractControl implements MovementSessionListener{

    private static final float SNAP_LIMIT = 1.0f;
    // Will snap a spatial if its further away than this value from its setpoint
    
    private final Object LOCK = new Object();
    // Used to synchronize updates/reads on setpoint 
    
    Vector3f setpoint; 
    // Börvärde in english
    
    public ConvergeControl(ClientMovementService service){
        service.addListener(this);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if(setpoint == null) return;
        
        Vector3f tempSetpoint;
        synchronized(LOCK){
            tempSetpoint = setpoint.clone();
        }
        
        
        CharacterControl character = getSpatial().getControl(CharacterControl.class);
        
        Vector3f currentPos = getSpatial().getLocalTranslation();
        Vector3f dif = currentPos.subtract(tempSetpoint);
             
        //METHOD 1: Takes a small fraction of difference towards the setpoint
        if(dif.length() > SNAP_LIMIT){
            character.warp(tempSetpoint);
        }else{
            character.warp(currentPos.add(dif.multLocal(0.1f * dif.length()/SNAP_LIMIT).negate()));
        }
        
        // METHOD 2: Changes the walkdirection based on the difference vector
        /*if(dif.length() > SNAP_LIMIT){
            System.out.println("Snapping, length = " + dif.length());
            character.setPhysicsLocation(tempSetpoint);
        }else{
            System.out.println("Linear, length = " + dif.length());
            //Vector3f walk = character.getWalkDirection().add(
            //        dif.negate().mult(tpf));
            //character.setWalkDirection(walk.add(dif.negate().mult(tpf)));
            Vector3f walk = character.getWalkDirection().add(dif).normalize().mult(EntityNode.MOVEMENT_SPEED*tpf);
            character.setWalkDirection(walk);
        }*/
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }

    @Override
    public void notifyPlayerMovement(List<PlayerMovement> playerMovements) {
        for(PlayerMovement pm : playerMovements){
            if(pm.id.equals(getSpatial().getName())){
                synchronized(LOCK){
                    setpoint = pm.location;
                }
                return;
            }
        }
    }
    
}
