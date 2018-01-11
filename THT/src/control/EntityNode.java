/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.LodGenerator;

/**
 * The spatial for a movable character
 * @author hannes
 */
public abstract class EntityNode extends Node{
        private static final Logger LOGGER = Logger.getLogger(EntityNode.class.getName());

    // TODO: Init variables for different trap status, i.e. isFrozen.
    CharacterControl charControl;
    Spatial model;
    AnimControl animationControl;
    AnimChannel animationChannel;
    
    BulletAppState bulletAppState;
    
    public float movementSpeed = NORMAL_MOVEMENT_SPEED;
    public static float SLOWED_MOVEMENT_SPEED = 1.0f;
    public static float NORMAL_MOVEMENT_SPEED = 3.0f;
    private long timer;
    private Boolean slowed = false;
    private final int slowTime = 3;
    
    public EntityNode(String name, Vector3f position, BulletAppState bulletAppState, Spatial model) {
        super(name);
        this.model = model;
        for (Spatial spatial : ((Node)model).getChildren()) {
            if (spatial instanceof Geometry) {
                Geometry g = (Geometry) spatial;
                System.out.println("Adding LOD to geom, triangle count: " + 
                g.getTriangleCount());
                LodGenerator lod = new LodGenerator(g);
                //lod.bakeLods(LodGenerator.TriangleReductionMethod.COLLAPSE_COST, 0.5f);
                
                lod.bakeLods(LodGenerator.TriangleReductionMethod.PROPORTIONAL,0.25f, 0.5f, 0.75f);

                LodControl lc = new LodControl();
                System.out.println("Distance tolerance: " + lc.getDistTolerance());
                lc.setDistTolerance(3);
                g.addControl(lc);
            }
        }
        
        this.bulletAppState = bulletAppState;
        initEntity(position);
    }
        
    /**
     * Sets up model and controllers that defines the behaviour of this entity
     * @param position 
     */
    public abstract void initEntity(Vector3f position);

    public Spatial getmodel(){
        return model;
    }
    
    /*public void changeModel(Vector3f pos){
        if (pos.distance(this.getWorldTranslation()) > 10) {
            this.detachChild(model);
        } else {
            if (this.getChild(model.getName()) == null){
                this.attachChild(model);
            }
        }
    }*/
    
    public void jumped(){
        charControl.jump();
    }
        
    public void slowDown(){
        if(!slowed){
            timer = System.currentTimeMillis();
            slowed = true;
            movementSpeed = SLOWED_MOVEMENT_SPEED;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(EntityNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    slowed = false;
                    movementSpeed = NORMAL_MOVEMENT_SPEED;
                    Vector3f wd = charControl.getWalkDirection();
                    if (wd.length() > 0) {
                        wd.normalizeLocal();
                        wd.multLocal(movementSpeed);
                    }
                }
            }).start();            
        }
    }
    
}    