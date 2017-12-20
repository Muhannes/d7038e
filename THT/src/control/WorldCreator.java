/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import api.models.Player;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class WorldCreator {
    private static final Logger LOGGER = Logger.getLogger(WorldCreator.class.getName());
    
    
    public static Node createPlayers(List<Player> listOfPlayers, BulletAppState bulletAppState, Material material){
        LOGGER.log(Level.INFO, "Initializing {0} number of players", listOfPlayers.size() );
        
        Node players = new Node("players");
        
        
        listOfPlayers.forEach(p -> {
            players.attachChild(createPlayer(Integer.toString(p.getID()), p.getPosition(), bulletAppState, material));
        });
        
        return players;
    }
    
    public static Spatial createPlayer(String name, Vector3f position, BulletAppState bulletAppState, Material material){
        LOGGER.log(Level.INFO, "Name: {0}, Position: {1}", new Object[]{name, position.toString()});
        
        Box mesh = new Box(0.2f, 0.4f, 0.2f); //Change to model 
        Geometry player = new Geometry(name, mesh);
        
        material.setColor("Color", ColorRGBA.Blue);
        player.setMaterial(material);
        player.setLocalTranslation(new Vector3f(-5.5f,5f, -9.5f));
        
        BoundingBox boundingBox = (BoundingBox) player.getWorldBound();
        float radius = boundingBox.getXExtent();
        float height = boundingBox.getYExtent();
        CapsuleCollisionShape shape = new CapsuleCollisionShape(radius, height);                
        CharacterControl charControl = new CharacterControl(shape, 1.0f); 
        player.addControl(charControl);
        
        if(bulletAppState == null){
            LOGGER.log(Level.SEVERE, "BulletAppState is null");   
            
        }
        
        if(bulletAppState.getPhysicsSpace() == null){
            LOGGER.log(Level.SEVERE, "physicsSpace is null");
        }
        
        bulletAppState.getPhysicsSpace().add(charControl);
        
        return player;
    }
    
    public static void addPhysicsToMap(BulletAppState bulletAppState, Spatial mapModel){ 
        Spatial walls = ((Node)mapModel).getChild("walls");        
        ((Node)walls).getChildren().forEach((wall) -> {                    
            RigidBodyControl b = new RigidBodyControl(
                   CollisionShapeFactory.createBoxShape(wall), 0); // 0 Mass = static
            
            b.setKinematic(true); // This for some reason makes the rigid align with the Mesh...
            
            wall.addControl(b);  
            
            bulletAppState.getPhysicsSpace().add(b);  
        });
        
        Spatial floors = ((Node)mapModel).getChild("floor");
        ((Node)floors).getChildren().forEach((floor) -> {
            RigidBodyControl b = new RigidBodyControl(0); // 0 Mass = static
            
            floor.addControl(b);

            bulletAppState.getPhysicsSpace().add(b);
        });
        
        LOGGER.log(Level.INFO, "Number of walls: {0}, Number of floors: {1}", 
                new Object[]{((Node)walls).getChildren().size(), ((Node)floors).getChildren().size()});
    }
}
