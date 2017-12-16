/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

/**
 *
 * @author hannes
 */
public interface GameStatsSessionListener {
    
    void notifyPlayerKilled(String victim, String killer);
    
    void notifyPlayerEscaped(String name);

}
