package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataCreationException;

/**
 * Exception thrown when attempting to create an entity with a duplicate key.
 */
public class DuplicateKeyException extends DataCreationException {
    
    public DuplicateKeyException(String message) {
        super(message);
    }
    
    public DuplicateKeyException(String entityType, String field, String value) {
        super(entityType, field + " '" + value + "' already exists");
    }
    
    public DuplicateKeyException(String entityType, String field, String value, Throwable cause) {
        super("Entity " + entityType + " with " + field + " '" + value + "' already exists", cause);
    }
} 