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

    public Player(int id) {
        this.id = id;
    }
    
    public int getID(){
        return id;
    }
}
