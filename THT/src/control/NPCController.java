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
public class NPCController implements PhysicsCollisionListener {
    private static final Random RANDOM = new Random();
    private Node root;
    private HostedMovementService hostedMovementService;
    private final List<Node> npcNodes = new ArrayList<>();
    private ScheduledExecutorService executor;
    
    
    public NPCController(Node root, HostedMovementService hostedMovementService, BulletAppState bulletAppState) {
        this.root = root;
        this.hostedMovementService = hostedMovementService;
        npcNodes.add((Node) root.getChild("0"));
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        startControlling();
    }
    
    private void startControlling(){
        executor = Executors.newScheduledThreadPool(npcNodes.size());
        for (Node npcNode : npcNodes) {
            executor.scheduleAtFixedRate(getRunnableController(npcNode.getControl(CharacterControl.class), npcNode.getName()), 
                    1, 1, TimeUnit.SECONDS);
        }
    }
    
    public void stopControlling(){
        executor.shutdownNow();
        npcNodes.clear();
    }
    
    private Runnable getRunnableController(final CharacterControl cc, final String id){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Vector3f newDir = new Vector3f(RANDOM.nextFloat() - 0.5f, 0, RANDOM.nextFloat() - 0.5f);
                //newDir = newDir.normalize().mult(NPC_MOVEMENT_SPEED);
                cc.setWalkDirection(newDir);
                cc.setViewDirection(newDir);
                hostedMovementService.playerUpdated(id);
            }
        };
        return r;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial a = event.getNodeA();
        Spatial b = event.getNodeB();
        System.out.println("A: " + a.getName());
        System.out.println("B: " + b.getName());
        if (a.getName().equals("Box") || b.getName().equals("Box")) {
            System.out.println("Collision with inner wall");
        }
    }
    
}
