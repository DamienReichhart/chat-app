package com.ap4.client.interfaces.controllers.handler;

import com.ap4.common.models.Message;

/**
 * Interface for handling message actions in the chat UI.
 * Implementations will provide the actual behavior for these actions.
 */
public interface MessageActionHandler {
    
    /**
     * Scrolls the message list to show a specific message.
     * @param message The message to scroll to
     */
    void scrollToMessage(Message message);
    
    /**
     * Pins a message in the current chat.
     * @param message The message to pin
     */
    void pinMessage(Message message);
    
    /**
     * Unpins a message in the current chat.
     * @param message The message to unpin
     */
    void unpinMessage(Message message);
    
    /**
     * Deletes a message from the current chat.
     * @param message The message to delete
     */
    void deleteMessage(Message message);
    
    /**
     * Refreshes the messages list to show updated content.
     */
    void refreshMessages();
} 