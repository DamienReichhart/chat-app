package com.ap4.client.services;

import com.ap4.client.exceptions.websocket.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles WebSocket exceptions by providing a centralized mechanism for
 * processing and responding to various types of WebSocket errors.
 */
public class WebSocketExceptionHandler {
    
    private static final Logger LOGGER = Logger.getLogger(WebSocketExceptionHandler.class.getName());
    
    /**
     * Handles a WebSocket exception, logs it, and returns an appropriate
     * user-friendly message.
     * 
     * @param exception The WebSocket exception to handle
     * @return A user-friendly error message
     */
    public String handleException(WebSocketException exception) {
        LOGGER.log(Level.WARNING, "WebSocket error: " + exception.getMessage(), exception);
        
        if (exception instanceof WebSocketConnectionException) {
            return handleConnectionException((WebSocketConnectionException) exception);
        } else if (exception instanceof WebSocketAuthenticationException) {
            return handleAuthenticationException((WebSocketAuthenticationException) exception);
        } else if (exception instanceof WebSocketMessageException) {
            return handleMessageException((WebSocketMessageException) exception);
        } else if (exception instanceof WebSocketSubscriptionException) {
            return handleSubscriptionException((WebSocketSubscriptionException) exception);
        } else if (exception instanceof WebSocketProtocolException) {
            return handleProtocolException((WebSocketProtocolException) exception);
        } else {
            return "A WebSocket error occurred: " + exception.getMessage();
        }
    }
    
    /**
     * Handles a WebSocket error message, attempts to interpret the message,
     * and returns an appropriate user-friendly message.
     * 
     * @param errorMessage The error message to handle
     * @return A user-friendly error message
     */
    public String handleMessage(String errorMessage) {
        LOGGER.log(Level.WARNING, "WebSocket error message: " + errorMessage);
        
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "An unknown WebSocket error occurred";
        }
        
        // Try to interpret common error patterns
        if (errorMessage.contains("Connection refused") || errorMessage.contains("Failed to connect")) {
            return "Could not connect to the chat server. Please check your internet connection and try again.";
        } else if (errorMessage.contains("Connection reset") || errorMessage.contains("Connection closed")) {
            return "The connection to the chat server was lost. Attempting to reconnect...";
        } else if (errorMessage.contains("Authentication") || errorMessage.contains("Unauthorized")) {
            return "You are not authorized to access this chat. Please log in again.";
        } else if (errorMessage.contains("Subscribe") || errorMessage.contains("Subscription")) {
            return "Could not join the chat room. Please try again later.";
        } else if (errorMessage.contains("Send") || errorMessage.contains("Message")) {
            return "Could not send your message. Please try again.";
        } else {
            return "An error occurred while communicating with the chat server: " + errorMessage;
        }
    }
    
    /**
     * Handles connection-related exceptions.
     * 
     * @param exception The connection exception
     * @return A user-friendly error message
     */
    private String handleConnectionException(WebSocketConnectionException exception) {
        WebSocketConnectionException.ConnectionState state = exception.getConnectionState();
        
        switch (state) {
            case CONNECTION_FAILED:
                return "Failed to connect to the chat server. Please check your internet connection and try again.";
            case CONNECTION_LOST:
                return "Lost connection to the chat server. Attempting to reconnect...";
            case CONNECTION_REFUSED:
                return "The chat server refused the connection. Please try again later.";
            case CONNECTION_TIMEOUT:
                return "Connection to the chat server timed out. Please check your internet connection.";
            default:
                return "A connection error occurred: " + exception.getMessage();
        }
    }
    
    /**
     * Handles authentication-related exceptions.
     * 
     * @param exception The authentication exception
     * @return A user-friendly error message
     */
    private String handleAuthenticationException(WebSocketAuthenticationException exception) {
        return "Authentication error: " + exception.getMessage() + ". Please log in again.";
    }
    
    /**
     * Handles message-related exceptions.
     * 
     * @param exception The message exception
     * @return A user-friendly error message
     */
    private String handleMessageException(WebSocketMessageException exception) {
        return "Could not send message: " + exception.getMessage() + ". Please try again.";
    }
    
    /**
     * Handles subscription-related exceptions.
     * 
     * @param exception The subscription exception
     * @return A user-friendly error message
     */
    private String handleSubscriptionException(WebSocketSubscriptionException exception) {
        WebSocketSubscriptionException.SubscriptionErrorType type = exception.getErrorType();
        
        switch (type) {
            case SUBSCRIBE_FAILED:
                return "Could not join the chat room. Please try again later.";
            case UNSUBSCRIBE_FAILED:
                return "Could not leave the chat room. Please try again later.";
            default:
                return "A subscription error occurred: " + exception.getMessage();
        }
    }
    
    /**
     * Handles protocol-related exceptions.
     * 
     * @param exception The protocol exception
     * @return A user-friendly error message
     */
    private String handleProtocolException(WebSocketProtocolException exception) {
        return "A communication error occurred: " + exception.getMessage();
    }
} 