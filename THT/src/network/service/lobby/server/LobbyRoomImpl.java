/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.service.gamesetup.SetupGameEvent;
import network.service.lobby.LobbyRoom;
import network.service.lobby.LobbySessionListener;
import network.service.login.Account;
import utils.eventbus.EventBus;

/**
 *
 * @author truls
 */
public class LobbyRoomImpl implements LobbyRoom {
    
    private static final int MAX_PLAYERS = 10;
    private static final Map<String, LobbyRoom> rooms = new HashMap<>();
    private static int chatIdCounter = 1;
    
    private final List<LobbySessionImpl> participants = new ArrayList<>();
    // Used to callback to each participant
    
    private final List<String> names = new ArrayList<>();
    // Name of players in this room;
    
    private final String name;
    // Lobby room name
    
    private final int max;
    // Maximum amount of players
    
    private int numberOfPlayers;
    // Current number of players
    
    private int playersReady;
    // Current number of players ready to play
    
    private final int chatId;
    // Chat that lobby participants will join
    
    private LobbyRoomImpl(String name, int max){
        this.name = name;
        this.max = max;
        this.chatId = chatIdCounter++;
    }
    
    private void decrementPlayersReady(){
        playersReady--;
    }
    
    static LobbyRoom getRoom(String name){
        LobbyRoom room = rooms.get(name);
        if(room == null){
            room = new LobbyRoomImpl(name, MAX_PLAYERS);
            rooms.put(name, room);
        }
        return room;
    }
    
    static List<LobbyRoom> getAllRooms(){
        List<LobbyRoom> temp = new ArrayList<>();
        rooms.values().forEach((r) -> {
            temp.add(new LobbyRoomMessage(r.getPlayers(), r.getName(), r.numberOfPlayers(), r.maxPlayers(), r.getChatId()));
        });
        return temp;
    }
    
    synchronized String join(LobbySessionImpl player){
        if(canJoin(player)){
            participants.forEach(p -> p.playerJoinedLobby(player.getName()));
            participants.add(player);
            names.add(player.getName());
            player.joinedLobby(new LobbyRoomMessage(names, name, numberOfPlayers, max, chatId));
            return name;
        }
        return null;
    }
    
    synchronized void leave(LobbySessionImpl player, boolean wasReady){
        names.remove(player.getName());
        participants.remove(player);
        participants.forEach(p -> p.playerLeftLobby(player.getName()));
        if (wasReady){
            decrementPlayersReady();
        }
        if(participants.isEmpty()) rooms.remove(name);
        // If no players left in lobby room, remove it
    }
    
    synchronized void ready(LobbySessionImpl player){
        participants.forEach(p -> p.playerReady(player.getName(), true));
     
        playersReady++;
        if(playersReady == participants.size()){
            List<Account> accounts = new ArrayList<>();
            participants.forEach(p -> accounts.add(p.getAccount()));
            List<LobbySessionListener> callbacks = new ArrayList<>();
            participants.forEach(p -> callbacks.add(p));
            EventBus.publish(new SetupGameEvent(accounts, callbacks), SetupGameEvent.class);
        }
    }
    
    private boolean canJoin(LobbySessionImpl player){
        if (participants.contains(player)) {
            return false;
        }
        return participants.size() < max;
    }
    
    @Override
    public synchronized List<String> getPlayers(){
        return names;
    }
    
    @Override
    public String getName(){
        return name;
    }
    
    @Override
    public int numberOfPlayers() {
        return numberOfPlayers;
    }

    @Override
    public int maxPlayers() {
        return max;
    }

    @Override
    public int getChatId() {
        return chatId;
    }

}
