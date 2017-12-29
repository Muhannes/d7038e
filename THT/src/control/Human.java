/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import control.action.Jump;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import network.service.movement.PlayerMovement;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author ted
 */
public class Human extends AbstractController implements ActionListener, AnalogListener{

    private Jump jump;
    
    public Boolean forward = false, backward = false, left = false, right = false, strafeLeft = false, strafeRight = false;
    public Boolean stopped = true;
    private final EntityNode self;
    private final AssetManager asset;
    private final SimpleApplication app;
    private Camera camera;
    
    
    private ClientMovementService clientMovementService;
    
    public Human(EntityNode player, SimpleApplication app, ClientMovementService clientMovementService){
        this.self = player;
        this.app = (SimpleApplication)app;
        this.asset = app.getAssetManager();
        this.camera = app.getCamera();
        this.clientMovementService = clientMovementService;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("jump")){ 
            self.charControl.jump();
        }
        if(name.equals("forward")){
            if(isPressed){
                forward = true;
            }else{
                forward = false;
            }
        }
        if(name.equals("backward")){
            if(isPressed){
                backward = true;
            }else{
                backward = false;
            }
        }
        if(name.equals("strafeLeft")){
            if(isPressed){
                strafeLeft = true;
            }else{
                strafeLeft = false;
            }
        }
        if(name.equals("strafeRight")){
            if(isPressed){
                strafeRight = true;
            }else{
                strafeRight = false;
            }
        } 
        if(!forward && !backward && !strafeLeft && !strafeRight && !left && !right){
            stopped = true;
        } else {
            stopped = false;
        }
        setNewMoveDirection();
        sendMovementToServer();
        
        if(name.equals("trap")){
            if(isPressed){
                
                createTrap();
            }
        }           
    }
    
    /**
     * Depending on what buttons are pressed, set a new movementDirection
     * Should always be called when updating speed or direction.
     */
    private void setNewMoveDirection(){
        Vector3f camDir = camera.getDirection().clone();
        Vector3f camLeft = camera.getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        
        Vector3f moveDirection = new Vector3f(Vector3f.ZERO);
        if (forward) {
            moveDirection.addLocal(camDir);
        }
        if (backward) {
            moveDirection.addLocal(camDir.negate());
        }
        if (strafeLeft) {
            moveDirection.addLocal(camLeft);
        }
        if (strafeRight) {
            moveDirection.addLocal(camLeft.negate());
        }
        
        moveDirection.normalizeLocal();
        moveDirection.multLocal(EntityNode.MOVEMENT_SPEED);

        Vector3f rotation = self.charControl.getWalkDirection();
        //self.rotate(rotation.x, 0.0f, rotation.z); //Rotate the body to where it's going
        self.setWalkDirection(moveDirection);
        if (forward || backward || strafeLeft || strafeRight) {
            Vector3f viewDirection = new Vector3f(moveDirection).normalize();
            viewDirection.y = 0;
            self.setViewDirection(viewDirection);
        }
    }
    
    /**
     * Sends information about model to server
     */
    private void sendMovementToServer(){         
        System.out.println("Sending new direction");
        PlayerMovement pm = new PlayerMovement(self.getName(), self.getLocalTranslation(),
                self.getWalkDirection(), self.getLocalRotation());
        clientMovementService.sendMessage(pm);
    }

    /**
     *
     */
    public void createTrap(){
        Box box = new Box(0.1f,0.1f,0.1f);
        Geometry geom = new Geometry("Box", box);
        Material material = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(material);
        geom.setLocalTranslation(self.getLocalTranslation());        
        app.getRootNode().attachChild(geom);
    }
    
    
    @Override
    public void initKeys(InputManager manager) {
        manager.addMapping("left", new KeyTrigger(KeyInput.KEY_Q));
        manager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        manager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        manager.addMapping("right", new KeyTrigger(KeyInput.KEY_E));
        manager.addMapping("strafeLeft", new KeyTrigger(KeyInput.KEY_A));
        manager.addMapping("strafeRight", new KeyTrigger(KeyInput.KEY_D));        
        manager.addMapping("trap", new KeyTrigger(KeyInput.KEY_F));        
        manager.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
        manager.addListener(this, "left", "right", "forward", "backward", "strafeLeft", "strafeRight", "jump", "trap");
    }

    /**
     * TODO: Make rotations here?
     * @param name
     * @param value
     * @param tpf 
     */
    @Override
    public void onAnalog(String name, float value, float tpf) {
        /*if(name.equals("left")){
            // TODO: Rotate left by tpf
            self.charControl.getWalkDirection();
        }
        if(name.equals("right")){
            self.charControl.getWalkDirection().addLocal(tpf, 0, 0);
        }*/
    }
}
