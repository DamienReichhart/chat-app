package com.ap4.client.exceptions.data;

/**
 * Exception thrown when entity deletion fails in the database.
 */
public class DataDeletionException extends DataAccessException {
    
    public DataDeletionException(String message) {
        super(message);
    }
    
    public DataDeletionException(String entityType, int id, Throwable cause) {
        super("Failed to delete " + entityType + " with ID " + id, cause);
    }
    
    public DataDeletionException(String entityType, int id, String detail) {
        super("Failed to delete " + entityType + " with ID " + id + ": " + detail);
    }
} 