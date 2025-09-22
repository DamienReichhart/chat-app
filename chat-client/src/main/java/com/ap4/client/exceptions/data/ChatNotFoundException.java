package com.ap4.client.exceptions.data;

/**
 * Exception thrown when trying to access a chat that doesn't exist.
 */
public class ChatNotFoundException extends DataAccessException {
    
    public ChatNotFoundException(int chatId) {
        super("Chat with ID " + chatId + " not found");
    }
    
    public ChatNotFoundException(String message) {
        super(message);
    }
    
    public ChatNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ChatNotFoundException(String field, String value) {
        super("Chat with " + field + " '" + value + "' not found");
    }
} 