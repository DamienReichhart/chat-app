package com.ap4.client.exceptions.websocket;

/**
 * Base exception class for WebSocket-related errors.
 * This serves as the parent class for all WebSocket-specific exceptions
 * in the application, providing common functionality.
 */
public class WebSocketException extends RuntimeException {
    
    private String endpoint;
    
    /**
     * Creates a new WebSocketException with the specified error message.
     * 
     * @param message The error message
     */
    public WebSocketException(String message) {
        super(message);
        this.endpoint = null;
    }
    
    /**
     * Creates a new WebSocketException with the specified error message and WebSocket endpoint.
     * 
     * @param message The error message
     * @param endpoint The WebSocket endpoint that caused the exception
     */
    public WebSocketException(String message, String endpoint) {
        super(message);
        this.endpoint = endpoint;
    }
    
    /**
     * Creates a new WebSocketException with the specified error message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
        this.endpoint = null;
    }
    
    /**
     * Creates a new WebSocketException with the specified error message, cause, and WebSocket endpoint.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param endpoint The WebSocket endpoint that caused the exception
     */
    public WebSocketException(String message, Throwable cause, String endpoint) {
        super(message, cause);
        this.endpoint = endpoint;
    }
    
    /**
     * Gets the WebSocket endpoint that caused the exception.
     * 
     * @return The WebSocket endpoint, or null if not specified
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * Sets the WebSocket endpoint that caused the exception.
     * 
     * @param endpoint The WebSocket endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
} 