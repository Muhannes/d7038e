/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.lobby.server;

import com.jme3.network.serializing.Serializable;
import java.util.List;
import network.service.lobby.LobbyRoom;

/**
 *
 * @author truls
 */
@Serializable
public class LobbyRoomMessage implements LobbyRoom{

    private List<String> players;
    private String name;
    private int numberOfPlayers;
    private int maxPlayers;
    private int chatId;
    
    public LobbyRoomMessage(){}
    
    public LobbyRoomMessage(List<String> players, String name, int numberOfPlayers, int maxPlayers, int chatId){
        this.players = players;
        this.name = name;
        this.numberOfPlayers = numberOfPlayers;
        this.maxPlayers = maxPlayers;
        this.chatId = chatId;
    }
    
    @Override
    public List<String> getPlayers() {
        return players;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int numberOfPlayers() {
        return numberOfPlayers;
    }

    @Override
    public int maxPlayers() {
        return maxPlayers;
    }

    @Override
    public int getChatId() {
        return chatId;
    }
    
}
