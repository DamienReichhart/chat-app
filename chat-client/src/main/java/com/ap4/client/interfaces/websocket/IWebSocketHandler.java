package com.ap4.client.interfaces.websocket;

import org.springframework.messaging.simp.stomp.StompSession;

import com.ap4.client.config.WebSocketConfig;
import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Interface defining methods for WebSocket communication handling
 */
public interface IWebSocketHandler {
    /**
     * Initialize the handler with configuration
     * @param config WebSocket configuration
     */
    void initialize(WebSocketConfig config);

    /**
     * Connect to the WebSocket server
     * @param user The current user
     */
    void connect(User user);

    /**
     * Disconnect from the WebSocket server
     */
    void disconnect();

    /**
     * Reset the connection state
     */
    void reset();

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
     * Add a WebSocket event listener
     * @param listener The listener to add
     */
    void addListener(WebSocketListener listener);

    /**
     * Remove a WebSocket event listener
     * @param listener The listener to remove
     */
    void removeListener(WebSocketListener listener);

    /**
     * Check if connected to the WebSocket server
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Get the current user
     * @return The current user
     */
    User getCurrentUser();

    /**
     * Handle successful connection
     * @param session The STOMP session
     */
    void handleSuccessfulConnection(StompSession session);

    /**
     * Handle transport error
     * @param session The STOMP session
     * @param exception The exception that occurred
     */
    void handleTransportError(StompSession session, Throwable exception);

    /**
     * Process a received message
     * @param dto The WebSocket message DTO
     */
    void processReceivedMessage(WebSocketMessageDTO dto);

    /**
     * Notify listeners of connection status change
     * @param connected The connection status
     */
    void notifyConnectionStatus(boolean connected);

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
