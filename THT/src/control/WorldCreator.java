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
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
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
        Spatial humanModel = assetManager.loadModel("Models/Oto/Oto.mesh.xml"); // robot
        Spatial monsterModel = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml"); // ninja
        Spatial monkeyModel = assetManager.loadModel("Models/Jaime/Jaime.j3o"); // monkey
        
        humanModel.scale(0.15f);
        monsterModel.scale(0.01f);
        listOfPlayers.forEach(p -> {
            Spatial model;
            if (p.getType() == EntityType.Human) {
                model = humanModel;
            } else if (p.getType() == EntityType.Monster) {
                model = monsterModel;
            } else {
                model = monkeyModel;
            }
            players.attachChild(createPlayer(Integer.toString(p.getID()), p.getPosition(), bulletAppState, model, p.getType()));
        });
        
        return players;
    }
    
    public static EntityNode createPlayer(String name, Vector3f position, BulletAppState bulletAppState, Spatial model, EntityType type){
        LOGGER.log(Level.INFO, "Name: {0}, Position: {1}", new Object[]{name, position.toString()});
        
        if (type == EntityType.Human) {
            return new HumanNode(name, position, bulletAppState, model);
            
        } else if (type == EntityType.Monster){
            return new MonsterNode(name, position, bulletAppState, model);
        } else if (type == EntityType.Monkey){
            LOGGER.log(Level.INFO, "Monkey name : " + name);
            return new MonkeyNode(name, position, bulletAppState, model);
        } else {
            return null;
        }
    }
    
    public static EntityNode createMonster(AssetManager assetManager, String name, BulletAppState bulletAppState){
        LOGGER.log(Level.INFO, "creating new monster");
        Spatial monsterModel = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml"); // ninja
        monsterModel.scale(0.01f);
        Spatial model = monsterModel.clone();
                    
        Vector3f tmpPos = new Vector3f(-3.16f, 2.0f, -8.9f); //monster spawn
        
        return new MonsterNode(name, tmpPos, bulletAppState, model);
    }
    
    public static void addPhysicsToMap(BulletAppState bulletAppState, Spatial mapModel){ 
        Spatial walls = ((Node)mapModel).getChild("walls");        
        ((Node)walls).getChildren().forEach((wall) -> {                    
            RigidBodyControl b = new RigidBodyControl(
                   CollisionShapeFactory.createBoxShape(wall), 0); // 0 Mass = static
            
            b.setKinematic(true); // This for some reason makes the rigid align with the Mesh...
            if (wall.getName().equals("longside")) {
                // Collisions with npc monkey
                b.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            }
            wall.addControl(b);  
            
            bulletAppState.getPhysicsSpace().add(b);  
        });
        
        Spatial floors = ((Node)mapModel).getChild("floor");
        ((Node)floors).getChildren().forEach((floor) -> {
            RigidBodyControl b = new RigidBodyControl(0); // 0 Mass = static
            // Collisions with npc monkey
            b.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            floor.addControl(b);

            bulletAppState.getPhysicsSpace().add(b);
        });
        
        LOGGER.log(Level.INFO, "Number of walls: {0}, Number of floors: {1}", 
                new Object[]{((Node)walls).getChildren().size(), ((Node)floors).getChildren().size()});
    }
}
