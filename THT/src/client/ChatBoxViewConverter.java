/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.controls.chatcontrol.ChatEntryModelClass;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 *
 * @author ted
 */
public class ChatBoxViewConverter implements ListBoxViewConverter<ChatEntryModelClass>{

    private static final String CHAT_ICON_LINE = "#chat-line-icon";
    private static final String CHAT_TEXT_LINE = "#chat-line-text";
    
    public ChatBoxViewConverter(){
        
    }
    
    @Override
    public final void display(final Element listBoxItem, final ChatEntryModelClass item) {
        final Element text = listBoxItem.findElementById(CHAT_TEXT_LINE);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementById(CHAT_ICON_LINE);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        if(item != null){
            textRenderer.setText(item.getLabel());
            iconRenderer.setImage(item.getIcon());
        } else {
            textRenderer.setText("");
            iconRenderer.setImage(null);
        }
    }

    @Override
    public final int getWidth(final Element listBoxItem, final ChatEntryModelClass item) {
        final Element text = listBoxItem.findElementById(CHAT_TEXT_LINE);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementById(CHAT_ICON_LINE);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        return (textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getLabel()) + ((item.getIcon() == null) ? 0 : item.getIcon().getWidth());
    }
    
}
