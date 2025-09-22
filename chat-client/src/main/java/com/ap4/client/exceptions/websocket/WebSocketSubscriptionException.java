package com.ap4.client.exceptions.websocket;

/**
 * Exception thrown when there is an error with WebSocket topic subscriptions.
 */
public class WebSocketSubscriptionException extends WebSocketException {
    
    private final String topic;
    private final SubscriptionErrorType errorType;
    
    /**
     * Enum representing the type of subscription error.
     */
    public enum SubscriptionErrorType {
        /** Failed to subscribe to topic */
        SUBSCRIBE_FAILED,
        
        /** Failed to unsubscribe from topic */
        UNSUBSCRIBE_FAILED,
        
        /** Subscription was rejected by server */
        SUBSCRIPTION_REJECTED,
        
        /** No permission to subscribe to topic */
        PERMISSION_DENIED,
        
        /** Topic does not exist */
        TOPIC_NOT_FOUND,
        
        /** Subscription limit exceeded */
        LIMIT_EXCEEDED,
        
        /** Unknown error */
        UNKNOWN
    }
    
    public WebSocketSubscriptionException(String message) {
        super(message);
        this.topic = null;
        this.errorType = SubscriptionErrorType.UNKNOWN;
    }
    
    public WebSocketSubscriptionException(String message, String topic) {
        super(message + (topic != null ? " (topic: " + topic + ")" : ""));
        this.topic = topic;
        this.errorType = SubscriptionErrorType.UNKNOWN;
    }
    
    public WebSocketSubscriptionException(String message, SubscriptionErrorType errorType) {
        super(message + (errorType != null ? " (error type: " + errorType + ")" : ""));
        this.topic = null;
        this.errorType = errorType;
    }
    
    public WebSocketSubscriptionException(String message, String topic, SubscriptionErrorType errorType) {
        super(message + (topic != null ? " (topic: " + topic + 
              (errorType != null ? ", error type: " + errorType : "") + ")" : ""));
        this.topic = topic;
        this.errorType = errorType;
    }
    
    public WebSocketSubscriptionException(String message, String topic, String endpoint) {
        super(message + (topic != null ? " (topic: " + topic + ")" : ""), endpoint);
        this.topic = topic;
        this.errorType = SubscriptionErrorType.UNKNOWN;
    }
    
    public WebSocketSubscriptionException(String message, String topic, SubscriptionErrorType errorType, 
                                         String endpoint) {
        super(message + (topic != null ? " (topic: " + topic + 
              (errorType != null ? ", error type: " + errorType : "") + ")" : ""), endpoint);
        this.topic = topic;
        this.errorType = errorType;
    }
    
    public WebSocketSubscriptionException(String message, Throwable cause) {
        super(message, cause);
        this.topic = null;
        this.errorType = SubscriptionErrorType.UNKNOWN;
    }
    
    public WebSocketSubscriptionException(String message, String topic, Throwable cause) {
        super(message + (topic != null ? " (topic: " + topic + ")" : ""), cause);
        this.topic = topic;
        this.errorType = SubscriptionErrorType.UNKNOWN;
    }
    
    public WebSocketSubscriptionException(String message, String topic, SubscriptionErrorType errorType, 
                                         Throwable cause) {
        super(message + (topic != null ? " (topic: " + topic + 
              (errorType != null ? ", error type: " + errorType : "") + ")" : ""), cause);
        this.topic = topic;
        this.errorType = errorType;
    }
    
    /**
     * Creates a new WebSocketSubscriptionException with the specified error type, topic, and message.
     * 
     * @param errorType The type of subscription error
     * @param topic The topic name
     * @param message The error message
     */
    public WebSocketSubscriptionException(SubscriptionErrorType errorType, String topic, String message) {
        super(message + (topic != null ? " (topic: " + topic + 
              (errorType != null ? ", error type: " + errorType : "") + ")" : ""));
        this.topic = topic;
        this.errorType = errorType;
    }
    
    /**
     * Creates a new WebSocketSubscriptionException with the specified error type, topic, message, and cause.
     * 
     * @param errorType The type of subscription error
     * @param topic The topic name
     * @param message The error message
     * @param cause The cause of the exception
     */
    public WebSocketSubscriptionException(SubscriptionErrorType errorType, String topic, String message, Exception cause) {
        super(message + (topic != null ? " (topic: " + topic + 
              (errorType != null ? ", error type: " + errorType : "") + ")" : ""), cause);
        this.topic = topic;
        this.errorType = errorType;
    }
    
    /**
     * Gets the topic associated with the subscription error.
     * 
     * @return The topic, or null if not specified
     */
    public String getTopic() {
        return topic;
    }
    
    /**
     * Gets the type of subscription error.
     * 
     * @return The subscription error type
     */
    public SubscriptionErrorType getErrorType() {
        return errorType;
    }
} 