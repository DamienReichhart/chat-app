package com.ap4.client.exceptions.websocket;

/**
 * Exception thrown when there is an error establishing or maintaining a WebSocket connection.
 */
public class WebSocketConnectionException extends WebSocketException {
    
    private final ConnectionState connectionState;
    
    /**
     * Enum representing the state of the connection when the exception occurred.
     */
    public enum ConnectionState {
        /** Connection attempt failed */
        CONNECTION_FAILED,
        
        /** Connection was established but then lost */
        CONNECTION_LOST,
        
        /** Connection was refused by the server */
        CONNECTION_REFUSED,
        
        /** Connection timed out */
        CONNECTION_TIMEOUT,
        
        /** Connection was closed normally */
        DISCONNECTED,
        
        /** Unknown connection state */
        UNKNOWN
    }
    
    public WebSocketConnectionException(String message) {
        super(message);
        this.connectionState = ConnectionState.UNKNOWN;
    }
    
    public WebSocketConnectionException(String message, ConnectionState connectionState) {
        super(message + (connectionState != null ? " (connection state: " + connectionState + ")" : ""));
        this.connectionState = connectionState;
    }
    
    public WebSocketConnectionException(String message, ConnectionState connectionState, String endpoint) {
        super(message + (connectionState != null ? " (connection state: " + connectionState + ")" : ""), endpoint);
        this.connectionState = connectionState;
    }
    
    public WebSocketConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.connectionState = ConnectionState.UNKNOWN;
    }
    
    public WebSocketConnectionException(String message, ConnectionState connectionState, Throwable cause) {
        super(message + (connectionState != null ? " (connection state: " + connectionState + ")" : ""), cause);
        this.connectionState = connectionState;
    }
    
    public WebSocketConnectionException(String message, ConnectionState connectionState, String endpoint, Throwable cause) {
        super(message + (connectionState != null ? " (connection state: " + connectionState + ")" : ""), cause);
        this.connectionState = connectionState;
        setEndpoint(endpoint);
    }
    
    /**
     * Creates a new WebSocketConnectionException with the specified message, connection state, and cause.
     * 
     * @param connectionState The connection state when the exception occurred
     * @param message The error message
     * @param cause The cause of the exception
     */
    public WebSocketConnectionException(ConnectionState connectionState, String message, Exception cause) {
        super(message + (connectionState != null ? " (connection state: " + connectionState + ")" : ""), cause);
        this.connectionState = connectionState;
    }
    
    /**
     * Gets the connection state when the exception occurred.
     * 
     * @return The connection state
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }
} 