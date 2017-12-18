/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author ted
 */
public class MovementSpace {
    
    public enum Movement{
        GLOBAL,
    }

    private final int id;
    // Uniquely identifies this chat space

    private final ArrayList<MovementSessionImpl> participants = new ArrayList<>();
    // A list of all participant of a chat

    static HashMap<Integer, MovementSpace> movementspaces = new HashMap<>();
    // A map over all chatspaces


    public MovementSpace(int id){
        this.id = id;
    }
       
    static void initDefualtMovementSpaces(){
        getMovementSpace(MovementSpace.Movement.GLOBAL.ordinal());
    }

    static MovementSpace getMovementSpace(int id){
        MovementSpace movement = movementspaces.get(id);
        if(movement == null){
            movement = new MovementSpace(id);
            movementspaces.put(id, movement);
        }
        return movement;
    }
    
    /**
     * Add a participant to this chatspace
     * @param participant Participant to add
     */
    void add(MovementSessionImpl participant){
        participants.forEach(p -> p.playerJoinedMovement(participant.getName(), id));
        participants.add(participant);
    }
    
    /**
     * Remove a participant from this chatspace
     * @param participant Participant to remove
     */
    void remove(MovementSessionImpl participant){
        participants.remove(participant);
        participants.forEach(p -> p.playerLeftMovement(participant.getName(), id));
    }
    
    ArrayList<MovementSessionImpl> getParticipants(){
        return participants;
    }
    
    void postMessage(MovementSessionImpl from, Vector3f location){
        //LOGGER.info("sending to : " + participants.size() + "clients. -> roomID: " + id);
        participants.forEach(p -> p.newMessage(location, id));
    }
    
    static void removeFromAll(MovementSessionImpl participant){
        movementspaces.forEach((i, space) -> space.remove(participant));
    }
    
}
