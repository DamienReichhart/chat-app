package com.ap4.client.exceptions.websocket;

/**
 * Exception thrown when there is an error sending or receiving WebSocket messages.
 */
public class WebSocketMessageException extends WebSocketException {
    
    private final String destination;
    private final MessageDirection direction;
    private final ErrorType errorType;
    
    /**
     * Enum representing the direction of the message when the error occurred.
     */
    public enum MessageDirection {
        /** Error occurred when sending a message */
        OUTGOING,
        
        /** Error occurred when receiving a message */
        INCOMING,
        
        /** Unknown direction */
        UNKNOWN
    }
    
    /**
     * Enum representing the type of message error.
     */
    public enum ErrorType {
        /** Failed to send message */
        SEND_FAILED,
        
        /** Failed to receive message */
        RECEIVE_FAILED,
        
        /** Invalid message format */
        INVALID_FORMAT,
        
        /** Message is too large */
        MESSAGE_TOO_LARGE,
        
        /** Rate limit exceeded */
        RATE_LIMITED,
        
        /** Message parsing error */
        PARSING_ERROR,
        
        /** Unknown error */
        UNKNOWN
    }
    
    public WebSocketMessageException(String message) {
        super(message);
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = ErrorType.UNKNOWN;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""));
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = errorType;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType, MessageDirection direction) {
        super(message + (errorType != null ? " (error type: " + errorType + 
              (direction != null ? ", direction: " + direction : "") + ")" : ""));
        this.destination = null;
        this.direction = direction;
        this.errorType = errorType;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType, MessageDirection direction, 
                                    String destination) {
        super(message + (errorType != null ? " (error type: " + errorType + 
              (direction != null ? ", direction: " + direction : "") + 
              (destination != null ? ", destination: " + destination : "") + ")" : ""));
        this.destination = destination;
        this.direction = direction;
        this.errorType = errorType;
    }
    
    public WebSocketMessageException(String message, String destination) {
        super(message + (destination != null ? " (destination: " + destination + ")" : ""));
        this.destination = destination;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = ErrorType.UNKNOWN;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType, MessageDirection direction, 
                                    String destination, String endpoint) {
        super(message + (errorType != null ? " (error type: " + errorType + 
              (direction != null ? ", direction: " + direction : "") + 
              (destination != null ? ", destination: " + destination : "") + ")" : ""), endpoint);
        this.destination = destination;
        this.direction = direction;
        this.errorType = errorType;
    }
    
    public WebSocketMessageException(String message, Throwable cause) {
        super(message, cause);
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = ErrorType.UNKNOWN;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType, Throwable cause) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""), cause);
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = errorType;
    }
    
    public WebSocketMessageException(String message, ErrorType errorType, MessageDirection direction, 
                                    String destination, Throwable cause) {
        super(message + (errorType != null ? " (error type: " + errorType + 
              (direction != null ? ", direction: " + direction : "") + 
              (destination != null ? ", destination: " + destination : "") + ")" : ""), cause);
        this.destination = destination;
        this.direction = direction;
        this.errorType = errorType;
    }
    
    /**
     * Creates a new WebSocketMessageException with the specified error type and message.
     * 
     * @param errorType The type of message error
     * @param message The error message
     */
    public WebSocketMessageException(ErrorType errorType, String message) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""));
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = errorType;
    }
    
    /**
     * Creates a new WebSocketMessageException with the specified error type, message, and cause.
     * 
     * @param errorType The type of message error
     * @param message The error message
     * @param cause The cause of the exception
     */
    public WebSocketMessageException(ErrorType errorType, String message, Exception cause) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""), cause);
        this.destination = null;
        this.direction = MessageDirection.UNKNOWN;
        this.errorType = errorType;
    }
    
    /**
     * Gets the destination of the message when the error occurred.
     * 
     * @return The message destination, or null if not specified
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * Gets the direction of the message when the error occurred.
     * 
     * @return The message direction
     */
    public MessageDirection getDirection() {
        return direction;
    }
    
    /**
     * Gets the type of message error.
     * 
     * @return The error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }
} 