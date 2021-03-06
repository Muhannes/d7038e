/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import network.service.movement.PlayerMovement;

/**
 * Utility functions for finding Area of Interest and players that are interested
 * in other players event
 * 
 * @author truls
 */
public class Filter {
    
    private static final Logger LOGGER = Logger.getLogger(Filter.class.getName());
    
    static boolean preProcessed = false;
    
    /**
     * This function returns all PlayerMovements that the given player is interested in.
     * @param player Given player 
     * @param movements Recently received player movements
     * @param rooms All rooms
     * @return A list of players
     */
    public static List<PlayerMovement> getPlayerMovements(Spatial player, List<PlayerMovement> movements, Node rooms){
        
        List<PlayerMovement> interestingMovements = new ArrayList<>();
        
        for(Spatial r : areaOfInterest(player, rooms)){
            for(PlayerMovement m : movements){
                Ray ray = new Ray(m.location, new Vector3f(0, -1, 0));
                if(r.getWorldBound().intersects(ray) && !interestingMovements.contains(m)){ // There is a possiblity that we intersect two rooms;
                    interestingMovements.add(m);
                }
            }
        }
        
        return interestingMovements;
    }
    
    /**
     * Given a player and a set of rooms, this function returns a set of rooms that
     * this player is interested in. A player is interested in a room if that room
     * is close enough to the player.
     * @param player
     * @param rooms
     * @return 
     */
    static List<Spatial> areaOfInterest(Spatial player, Node rooms){
        
        if(!preProcessed) preProcess(rooms);
        
        List<Spatial> temp = new ArrayList<>();
        
        Ray ray = new Ray(player.getLocalTranslation(), new Vector3f(0, -1, 0));
        // This ray points down into the ground and goes through the player
        
        for(Spatial r : rooms.getChildren()){
            if(r.getWorldBound().intersects(ray)){
                temp.addAll(getNeighbourRooms(r, rooms));
                temp.add(r);
                return temp;
            }
        }
        
        return temp;
    }
    
    
    
    /**
     * Find all rooms that are neighbour to the given room. Two rooms are neighbours
     * if they intersect each other.
     * @param room Room to find neighbours for
     * @param rooms 
     * @return A list of rooms that are neighbours to the given room
     */
    private static List<Spatial> getNeighbourRooms(Spatial room, Node rooms){
        List<Spatial> temp = new ArrayList<>();
        
        for(Spatial r : rooms.getChildren()){
            if(room.getWorldBound().intersects(r.getWorldBound()) && !room.equals(r)){
                temp.add(r);
            }
        }
        
        return temp;
    }
    
    /**
     * By slightly scaling the rooms we make the rooms intersecting eachother
     * which is required by the getNeighbourRooms() method
     * @param rooms 
     */
    private static void preProcess(Node rooms){
        rooms.getChildren().forEach(r -> r.scale(1.1f));
        preProcessed = true;
    }
    
}
