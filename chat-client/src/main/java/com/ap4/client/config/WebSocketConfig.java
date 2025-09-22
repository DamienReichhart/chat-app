package com.ap4.client.config;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class for WebSocket connections.
 * Provides centralized access to WebSocket-related settings.
 */
public class WebSocketConfig {
    private static final Logger logger = LogManager.getLogger(WebSocketConfig.class);
    
    /**
     * Default WebSocket server URL if not specified in properties.
     */
    private static final String DEFAULT_SERVER_URL = "ws://localhost:8080/chat";
    
    /**
     * Default WebSocket connection timeout in milliseconds.
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    
    /**
     * Default WebSocket reconnect attempt limit.
     */
    private static final int DEFAULT_RECONNECT_ATTEMPTS = 5;
    
    /**
     * Properties containing WebSocket connection settings.
     */
    private final Properties properties;
    
    /**
     * Creates a new WebSocketConfig with the specified properties.
     * 
     * @param properties Properties containing WebSocket settings
     */
    public WebSocketConfig(Properties properties) {
        this.properties = properties;
        logger.debug("WebSocket configuration initialized");
    }
    
    /**
     * Gets the WebSocket server URL.
     * 
     * @return The configured server URL or default if not specified
     */
    public String getServerUrl() {
        return properties.getProperty("websocket.url", DEFAULT_SERVER_URL);
    }
    
    /**
     * Gets the connection timeout in milliseconds.
     * 
     * @return The configured connection timeout or default if not specified
     */
    public int getConnectionTimeout() {
        try {
            String timeoutStr = properties.getProperty("websocket.timeout");
            if (timeoutStr != null) {
                return Integer.parseInt(timeoutStr);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid WebSocket timeout value in configuration, using default", e);
        }
        return DEFAULT_CONNECTION_TIMEOUT;
    }
    
    /**
     * Gets the maximum number of reconnect attempts.
     * 
     * @return The configured reconnect attempt limit or default if not specified
     */
    public int getReconnectAttempts() {
        try {
            String attemptsStr = properties.getProperty("websocket.reconnect.attempts");
            if (attemptsStr != null) {
                return Integer.parseInt(attemptsStr);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid WebSocket reconnect attempts value in configuration, using default", e);
        }
        return DEFAULT_RECONNECT_ATTEMPTS;
    }
    
    /**
     * Gets whether automatic reconnect is enabled.
     * 
     * @return True if automatic reconnect is enabled, false otherwise
     */
    public boolean isAutoReconnectEnabled() {
        String autoReconnectStr = properties.getProperty("websocket.reconnect.auto");
        return Boolean.parseBoolean(autoReconnectStr);
    }
} 