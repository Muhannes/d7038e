/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;  
import com.jme3.renderer.Camera;
import control.EntityNode;
import control.animation.MonsterAnimationControl;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;
import com.sun.istack.internal.logging.Logger;
import control.EntityNode;
import java.util.logging.Level;

/**
 * This control handles how a monster should react to keyboard input.
 * 
 * @author truls
 * @author ted
 * @author hannes
 */
public class MonsterInputControl extends AbstractInputControl{

    private static final Logger LOGGER = Logger.getLogger(MonsterInputControl.class);

    private CharacterControl character;

    // Physical body that we use to control movment of spatial
    
    private Camera camera;
    // Camera chasing the player
    
    private Vector3f moveDirection;
    // Used to set walking direction
    
    private Vector3f camDir;
    // Used to set new movement direction
    
    private Vector3f camLeft;
    // Used to set new movement direction
    
    private final float updatePeriod = 0.1f;
    private float lastUpdate = 0f;
    
    private EntityNode self;

    public MonsterInputControl(EntityNode self, ClientMovementService movementService, ClientGameStatsService gameStatsService) {
        super(movementService, gameStatsService);
        this.self = self;
        this.camera = camera;
        this.moveDirection = new Vector3f(0, 0, 0);

    }

    @Override
    public void initKeys(InputManager manager) {
       super.initKeys(manager);
       manager.addMapping("slash", new KeyTrigger(KeyInput.KEY_F));
       manager.addListener(this, "slash");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        super.onAction(name, isPressed, tpf);
        if (name.equals("slash") && isPressed) {
            getSpatial().getControl(MonsterAnimationControl.class).swordSlash();
        }
    }
}
