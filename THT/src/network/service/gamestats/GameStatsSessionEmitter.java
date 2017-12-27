/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamestats;

/**
 *
 * @author ted
 */
public interface GameStatsSessionEmitter {
    
    void addSessions(GameStatsSession session);
    
    void removeSessions(GameStatsSession session);
    
}
