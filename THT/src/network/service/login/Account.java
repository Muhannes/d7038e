/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.login;

/**
 *
 * @author hannes
 */
public class Account {
    
    public final String name;
    public final int id;
    public final String key;

    public Account(String name, int id, String key) {
        this.name = name;
        this.id = id;
        this.key = key;
    }
    
    public boolean isEqual(int id, String key){
        return this.id == id && (this.key == null ? key == null : this.key.equals(key));
    }
    
}
