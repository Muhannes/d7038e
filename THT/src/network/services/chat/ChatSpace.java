/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.chat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author truls
 */
public class ChatSpace {
    
    public enum Chat{
        GLOBAL,
    }
    
    private final int id;
    // Uniquely identifies this chat space
    
    private ArrayList<ChatSessionImpl> participants;
    // A list of all participant of a chat
    
    static HashMap<Integer, ChatSpace> chatspaces = new HashMap<>();
    // A map over all chatspaces
    
    private ChatSpace(int id){
        this.id = id;
        participants = new ArrayList<>();
    }
    
    static void initDefualtChatSpaces(){
        getChatSpace(Chat.GLOBAL.ordinal());
    }
    
    /**
     * Get the chatspace with the given ID. If there is 
     * no chat space with the given ID, a empty chatspace will be created
     * with the given ID and returned.
     * @param id ID of the chatspace
     * @return Chatspace with the given ID.
     */
    static ChatSpace getChatSpace(int id){
        ChatSpace chat = chatspaces.get(id);
        if(chat == null){
            chat = new ChatSpace(id);
            chatspaces.put(id, chat);
        }
        return chat;
    }
    
    /**
     * Add a participant to this chatspace
     * @param participant Participant to add
     */
    void add(ChatSessionImpl participant){
        participants.forEach(p -> p.playerJoinedChat(participant.getName(), id));
        participants.add(participant);
    }
    
    /**
     * Remove a participant from this chatspace
     * @param participant Participant to remove
     */
    void remove(ChatSessionImpl participant){
        System.out.println(participant);
        System.out.println(participants.toString());
        participants.remove(participant);
        participants.forEach(p -> p.playerLeftChat(participant.getName(), id));
    }
    
    ArrayList<ChatSessionImpl> getParticipants(){
        return participants;
    }
    
    void postMessage(ChatSessionImpl from, String message){
        participants.forEach(p -> p.newMessage(message, id));
    }
    
    static void removeFromAll(ChatSessionImpl participant){
        chatspaces.forEach((i, space) -> space.remove(participant));
    }
    
}
