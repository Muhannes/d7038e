/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import api.models.Entity;

/**
 *
 * @author ted
 */
public interface Command {
    
    void execute(Entity entity);
    
}