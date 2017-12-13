/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 *
 * @author ted
 */
public class Entity {
    
    private int id;
    private AssetManager asset;
    private Vector3f placement;
    private Geometry geometry;
    private Material mat;
    private CapsuleCollisionShape shape;
    private CharacterControl control;
    private BulletAppState bulletAppState;
    
    public Entity(){}
    
    public Entity(AssetManager asset, Vector3f placement, int id){
        this.asset = asset;
        this.placement = placement;
        this.id = id;
        
        Box box = new Box(1,1,1);
        mat = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geometry = new Geometry("player" + id, box);
        geometry.setLocalTranslation(placement);
        geometry.setMaterial(mat);
        
        BoundingBox boundingBox = (BoundingBox) geometry.getWorldBound();
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        shape = new CapsuleCollisionShape(radius, height);                
        control = new CharacterControl(shape, 1.0f);    
        
    }
    
    public void setNewPlacement(Vector3f newPlacement){
        this.placement = newPlacement;
    }
    
    public CharacterControl getController(){
        return control;
    }
    
    public Geometry getGeometry(){
        return geometry;
    }
    
    public int getId(){
        return id;
    }
    
    public void setColor(ColorRGBA color){
        mat.setColor("Color", color);         
    }
    
}
