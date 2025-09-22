package com.ap4.client.exceptions.data;

/**
 * Exception thrown when entity data fails validation constraints.
 */
public class InvalidDataException extends DataAccessException {
    
    public InvalidDataException(String message) {
        super(message);
    }
    
    public InvalidDataException(String entityType, String field, String reason) {
        super("Invalid " + entityType + " data: Field '" + field + "' " + reason);
    }
    
    public InvalidDataException(String entityType, String reason) {
        super("Invalid " + entityType + " data: " + reason);
    }
    
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
} 