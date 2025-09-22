package com.ap4.client.exceptions.websocket;

/**
 * Exception thrown when there is a WebSocket protocol error.
 */
public class WebSocketProtocolException extends WebSocketException {
    
    private final ProtocolErrorType errorType;
    private final String details;
    
    /**
     * Enum representing the type of protocol error.
     */
    public enum ProtocolErrorType {
        /** Invalid frame received */
        INVALID_FRAME,
        
        /** Protocol violation */
        PROTOCOL_VIOLATION,
        
        /** Frame size exceeded */
        FRAME_SIZE_EXCEEDED,
        
        /** Frame sent with invalid format */
        INVALID_FORMAT,
        
        /** Incorrect message sequence */
        INVALID_SEQUENCE,
        
        /** Unsupported protocol extension */
        UNSUPPORTED_EXTENSION,
        
        /** Compression error */
        COMPRESSION_ERROR,
        
        /** Encoding error */
        ENCODING_ERROR,
        
        /** Unknown error */
        UNKNOWN
    }
    
    public WebSocketProtocolException(String message) {
        super(message);
        this.errorType = ProtocolErrorType.UNKNOWN;
        this.details = null;
    }
    
    public WebSocketProtocolException(String message, ProtocolErrorType errorType) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""));
        this.errorType = errorType;
        this.details = null;
    }
    
    public WebSocketProtocolException(String message, ProtocolErrorType errorType, String details) {
        super(message + (errorType != null ? " (error type: " + errorType +
              (details != null ? ", details: " + details : "") + ")" : ""));
        this.errorType = errorType;
        this.details = details;
    }
    
    public WebSocketProtocolException(String message, String endpoint) {
        super(message, endpoint);
        this.errorType = ProtocolErrorType.UNKNOWN;
        this.details = null;
    }
    
    public WebSocketProtocolException(String message, ProtocolErrorType errorType, String details, String endpoint) {
        super(message + (errorType != null ? " (error type: " + errorType +
              (details != null ? ", details: " + details : "") + ")" : ""), endpoint);
        this.errorType = errorType;
        this.details = details;
    }
    
    public WebSocketProtocolException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ProtocolErrorType.UNKNOWN;
        this.details = null;
    }
    
    public WebSocketProtocolException(String message, ProtocolErrorType errorType, Throwable cause) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""), cause);
        this.errorType = errorType;
        this.details = null;
    }
    
    public WebSocketProtocolException(String message, ProtocolErrorType errorType, String details, Throwable cause) {
        super(message + (errorType != null ? " (error type: " + errorType +
              (details != null ? ", details: " + details : "") + ")" : ""), cause);
        this.errorType = errorType;
        this.details = details;
    }
    
    /**
     * Gets the type of protocol error.
     * 
     * @return The protocol error type
     */
    public ProtocolErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Gets additional details about the protocol error.
     * 
     * @return Additional details, or null if not specified
     */
    public String getDetails() {
        return details;
    }
} 