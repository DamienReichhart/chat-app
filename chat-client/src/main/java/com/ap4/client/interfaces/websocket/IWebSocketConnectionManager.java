package com.ap4.client.interfaces.websocket;

import com.ap4.client.config.WebSocketConfig;
import com.ap4.client.exceptions.websocket.WebSocketConnectionException;
import com.ap4.common.models.User;
import org.springframework.messaging.simp.stomp.StompSession;

/**
 * Interface defining methods for WebSocket connection management.
 * This interface is responsible for establishing, maintaining, and terminating WebSocket connections.
 */
public interface IWebSocketConnectionManager {
    /**
     * Initialize the connection manager with configuration
     * @param config WebSocket configuration
     */
    void initialize(WebSocketConfig config);

    /**
     * Connect to the WebSocket server
     * @param user The current user
     * @throws WebSocketConnectionException if connection fails
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
     * Get the current STOMP session
     * @return The current STOMP session
     */
    StompSession getStompSession();

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
     * Add a connection listener
     * @param listener The listener to add
     */
    void addConnectionListener(ConnectionListener listener);

    /**
     * Remove a connection listener
     * @param listener The listener to remove
     */
    void removeConnectionListener(ConnectionListener listener);

    /**
     * Notify listeners of connection status change
     * @param connected The connection status
     */
    void notifyConnectionStatus(boolean connected);
} 