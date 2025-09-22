package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when trying to access a message that doesn't exist.
 */
public class MessageNotFoundException extends DataAccessException {
    
    public MessageNotFoundException(int messageId) {
        super("Message with ID " + messageId + " not found");
    }
    
    public MessageNotFoundException(String message) {
        super(message);
    }
    
    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 