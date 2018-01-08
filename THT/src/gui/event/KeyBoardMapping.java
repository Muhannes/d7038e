/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.event;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;

/**
 *
 * @author truls
 */
public class KeyBoardMapping implements NiftyInputMapping{

    @Override
    public NiftyInputEvent convert(KeyboardInputEvent kie) {
        if(kie.isKeyDown()){
            if(kie.getKey() == KeyboardInputEvent.KEY_RETURN){ 
                return new EnterEvent();                
            }
            if(kie.getKey() == KeyboardInputEvent.KEY_P){
                return new PEvent();
            }
        }
        return null;
    }
}
