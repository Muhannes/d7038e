/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.converge;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.List;
import network.service.movement.MovementSession;
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

    Vector3f setpoint; 
    // Börvärde in english
    
    public ConvergeControl(ClientMovementService service){
        service.addListener(this);
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        // TODO: Do convergence
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }

    @Override
    public void newMessage(List<PlayerMovement> playerMovements) {
        for(PlayerMovement pm : playerMovements){
            if(pm.id.equals(getSpatial().getName())){
                setpoint = pm.location;
                
                /*Vector3f dif = getSpatial().getLocalTranslation().subtract(setpoint);
                System.out.println("newMessage:");
                System.out.println("Local: " + getSpatial().getLocalTranslation().toString());
                System.out.println("Setpoint: " + setpoint.toString());
                System.out.println("Dif:" + dif.toString());*/
                return;
            }
        }
    }
    
}
