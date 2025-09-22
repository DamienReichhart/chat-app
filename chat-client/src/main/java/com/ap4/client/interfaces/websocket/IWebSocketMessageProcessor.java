package com.ap4.client.interfaces.websocket;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;

/**
 * Interface defining methods for WebSocket message processing.
 * This interface is responsible for processing incoming messages and preparing outgoing messages.
 */
public interface IWebSocketMessageProcessor {
    /**
     * Process a chat message received from WebSocket
     * @param dto The received WebSocketMessageDTO containing a chat message
     */
    void processChatMessage(WebSocketMessageDTO dto);
    
    /**
     * Process a join chat event received from WebSocket
     * @param dto The received WebSocketMessageDTO containing join information
     */
    void processJoinChatEvent(WebSocketMessageDTO dto);
    
    /**
     * Process a leave chat event received from WebSocket
     * @param dto The received WebSocketMessageDTO containing leave information
     */
    void processLeaveChatEvent(WebSocketMessageDTO dto);
    
    /**
     * Process a pin message event received from WebSocket
     * @param dto The received WebSocketMessageDTO containing pin information
     */
    void processPinMessageEvent(WebSocketMessageDTO dto);
    
    /**
     * Process an unpin message event received from WebSocket
     * @param dto The received WebSocketMessageDTO containing unpin information
     */
    void processUnpinMessageEvent(WebSocketMessageDTO dto);
    
    /**
     * Process a delete message event received from WebSocket
     * @param dto The received WebSocketMessageDTO containing delete information
     */
    void processDeleteMessageEvent(WebSocketMessageDTO dto);
    
    /**
     * Join a chat room
     * @param chat The chat to join
     */
    void joinChat(Chat chat);

    /**
     * Leave a chat room
     * @param chat The chat to leave
     */
    void leaveChat(Chat chat);

    /**
     * Send a message
     * @param message The message to send
     */
    void sendMessage(Message message);

    /**
     * Send a message DTO to a specific destination
     * @param dto The message DTO
     * @param destination The destination path
     */
    void sendMessageDTO(WebSocketMessageDTO dto, String destination);

    /**
     * Pin a message in a chat
     * @param message The message to pin
     */
    void pinMessage(Message message);

    /**
     * Unpin a message in a chat
     * @param message The message to unpin
     */
    void unpinMessage(Message message);

    /**
     * Delete a message
     * @param message The message to delete
     */
    void deleteMessage(Message message);

    /**
     * Add a message listener
     * @param listener The listener to add
     */
    void addMessageListener(MessageListener listener);

    /**
     * Remove a message listener
     * @param listener The listener to remove
     */
    void removeMessageListener(MessageListener listener);

    /**
     * Notify listeners of message received
     * @param message The received message
     */
    void notifyMessageReceived(Message message);

    /**
     * Notify listeners of error
     * @param errorMessage The error message
     */
    void notifyError(String errorMessage);
} 