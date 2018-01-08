/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import network.service.movement.server.HostedMovementService;

/**
 *
 * @author hannes
 */
public class NPCController implements PhysicsCollisionListener{
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private Node root;
    private HostedMovementService hostedMovementService;
    private ScheduledExecutorService executor;
    private BulletAppState bulletAppState;
    
    
    public NPCController(Node root, HostedMovementService hostedMovementService, BulletAppState bulletAppState) {
        this.root = root;
        this.hostedMovementService = hostedMovementService;
        this.bulletAppState = bulletAppState;
        
        this.bulletAppState.getPhysicsSpace().addCollisionListener(this);
        startControlling();
    }
    
    private void startControlling(){
        
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(getRunnableController(), 1, 1, TimeUnit.SECONDS);
        
    }
    
    public void stopControlling(){
        executor.shutdownNow();
        bulletAppState.getPhysicsSpace().removeCollisionListener(this);
    }
    
    private Runnable getRunnableController(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Spatial spatial : ((Node)root.getChild("playersNode")).getChildren()) {
                    if (spatial instanceof MonkeyNode) {
                        Vector3f newDir = new Vector3f(RANDOM.nextFloat() - 0.5f, 0, RANDOM.nextFloat() - 0.5f);
                        //newDir = newDir.normalize().mult(NPC_MOVEMENT_SPEED);
                        CharacterControl cc = spatial.getControl(CharacterControl.class);
                        cc.setWalkDirection(newDir);
                        cc.setViewDirection(newDir);
                        hostedMovementService.playerUpdated(spatial.getName());
                    }
                }
            }
        };
        return r;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial a = event.getNodeA();
        Spatial b = event.getNodeB();
        if (a.getName().equals("longside") && b instanceof MonkeyNode) {
            turnAround(b.getControl(CharacterControl.class), b.getName());
        } else if (b.getName().equals("longside") && a instanceof MonkeyNode) {
            turnAround(a.getControl(CharacterControl.class), a.getName());
            
        }
    }
    
    private void turnAround(CharacterControl cc, String id){
        cc.setWalkDirection(cc.getWalkDirection().negate());
        cc.setViewDirection(cc.getWalkDirection());
        hostedMovementService.playerUpdated(id);
    }
    
}
