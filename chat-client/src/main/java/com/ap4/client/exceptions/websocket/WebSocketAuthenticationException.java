package com.ap4.client.exceptions.websocket;

/**
 * Exception thrown when there is an authentication or authorization error
 * with a WebSocket connection.
 */
public class WebSocketAuthenticationException extends WebSocketException {
    
    private final String username;
    private final FailureReason failureReason;
    
    /**
     * Enum representing the reason for authentication failure.
     */
    public enum FailureReason {
        /** Invalid username or password */
        INVALID_CREDENTIALS,
        
        /** Session has expired */
        EXPIRED_SESSION,
        
        /** Authentication token is missing */
        MISSING_TOKEN,
        
        /** Authentication token is invalid */
        INVALID_TOKEN,
        
        /** User is not authorized to access resource */
        AUTHORIZATION_FAILED,
        
        /** Unknown reason */
        UNKNOWN
    }
    
    public WebSocketAuthenticationException(String message) {
        super(message);
        this.username = null;
        this.failureReason = FailureReason.UNKNOWN;
    }
    
    public WebSocketAuthenticationException(String message, FailureReason failureReason) {
        super(message + (failureReason != null ? " (failure reason: " + failureReason + ")" : ""));
        this.username = null;
        this.failureReason = failureReason;
    }
    
    public WebSocketAuthenticationException(String message, String username) {
        super(message + (username != null ? " (username: " + username + ")" : ""));
        this.username = username;
        this.failureReason = FailureReason.UNKNOWN;
    }
    
    public WebSocketAuthenticationException(String message, String username, FailureReason failureReason) {
        super(message + (username != null ? " (username: " + username + 
              (failureReason != null ? ", failure reason: " + failureReason : "") + ")" : ""));
        this.username = username;
        this.failureReason = failureReason;
    }
    
    public WebSocketAuthenticationException(String message, String username, FailureReason failureReason, 
                                           String endpoint) {
        super(message + (username != null ? " (username: " + username + 
              (failureReason != null ? ", failure reason: " + failureReason : "") + ")" : ""), endpoint);
        this.username = username;
        this.failureReason = failureReason;
    }
    
    public WebSocketAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.username = null;
        this.failureReason = FailureReason.UNKNOWN;
    }
    
    public WebSocketAuthenticationException(String message, String username, Throwable cause) {
        super(message + (username != null ? " (username: " + username + ")" : ""), cause);
        this.username = username;
        this.failureReason = FailureReason.UNKNOWN;
    }
    
    public WebSocketAuthenticationException(String message, String username, FailureReason failureReason, 
                                           Throwable cause) {
        super(message + (username != null ? " (username: " + username + 
              (failureReason != null ? ", failure reason: " + failureReason : "") + ")" : ""), cause);
        this.username = username;
        this.failureReason = failureReason;
    }
    
    /**
     * Gets the username associated with the authentication failure.
     * 
     * @return The username, or null if not specified
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the reason for the authentication failure.
     * 
     * @return The failure reason
     */
    public FailureReason getFailureReason() {
        return failureReason;
    }
} 