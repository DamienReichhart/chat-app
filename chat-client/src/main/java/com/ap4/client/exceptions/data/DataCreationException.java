package com.ap4.client.exceptions.data;

/**
 * Exception thrown when entity creation fails in the database.
 */
public class DataCreationException extends DataAccessException {
    
    public DataCreationException(String message) {
        super(message);
    }
    
    public DataCreationException(String entityType, Throwable cause) {
        super("Failed to create " + entityType + " entity", cause);
    }
    
    public DataCreationException(String entityType, String detail) {
        super("Failed to create " + entityType + " entity: " + detail);
    }
} 