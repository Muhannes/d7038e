/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import api.models.EntityType;
import api.models.Player;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.blender.BlenderModelLoader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class WorldCreator {
    private static final Logger LOGGER = Logger.getLogger(WorldCreator.class.getName());
    
    
    public static Node createPlayers(List<Player> listOfPlayers, BulletAppState bulletAppState, AssetManager assetManager){
        LOGGER.log(Level.INFO, "Initializing {0} number of players", listOfPlayers.size() );
        Node players = new Node("players");
        // TODO: make different models for each character type
        Spatial monsterModel = assetManager.loadModel("Models/Oto/Oto.mesh.xml"); // robot
        Spatial humanModel = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml"); // ninja
        humanModel.scale(0.05f);
        monsterModel.scale(0.15f);
        listOfPlayers.forEach(p -> {
            Spatial model = (p.getType() == EntityType.Human) ? humanModel.clone() : monsterModel.clone();
            players.attachChild(createPlayer(Integer.toString(p.getID()), p.getPosition(), bulletAppState, model));
        });
        
        return players;
    }
    
    public static EntityNode createPlayer(String name, Vector3f position, BulletAppState bulletAppState, Spatial model){
        LOGGER.log(Level.INFO, "Name: {0}, Position: {1}", new Object[]{name, position.toString()});
        Vector3f tmpPos = new Vector3f(-5.5f,5f, -9.5f);
        return new EntityNode(name, tmpPos, bulletAppState, model);
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
