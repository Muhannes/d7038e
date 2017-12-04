/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

/**
 *
 * @author truls
 */
public class Player {
    private final int id;
    private String name;
    private boolean ready;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.ready = false;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public int getID(){
        return id;
    }
    
    public void setReady(boolean ready){
        this.ready = ready;
    }
    
    public boolean isReady(){
        return this.ready;
    }
    
}
