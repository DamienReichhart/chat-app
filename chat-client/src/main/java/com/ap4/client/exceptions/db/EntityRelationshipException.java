package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when entity relationships are violated.
 */
public class EntityRelationshipException extends DataAccessException {
    
    public EntityRelationshipException(String message) {
        super(message);
    }
    
    public EntityRelationshipException(String sourceEntity, String targetEntity, String reason) {
        super("Relationship violation between " + sourceEntity + " and " + targetEntity + ": " + reason);
    }
    
    public EntityRelationshipException(String sourceEntity, int sourceId, String targetEntity, int targetId, String operation) {
        super("Cannot " + operation + " " + sourceEntity + " (ID: " + sourceId + ") with " + 
              targetEntity + " (ID: " + targetId + ") due to relationship constraints");
    }
    
    public EntityRelationshipException(String message, Throwable cause) {
        super(message, cause);
    }
} 