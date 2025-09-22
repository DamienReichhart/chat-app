package com.ap4.client.exceptions.data;

/**
 * Exception thrown when entity update fails in the database.
 */
public class DataUpdateException extends DataAccessException {
    
    public DataUpdateException(String message) {
        super(message);
    }
    
    public DataUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataUpdateException(String entityType, int id, Throwable cause) {
        super("Failed to update " + entityType + " with ID " + id, cause);
    }
    
    public DataUpdateException(String entityType, int id, String detail) {
        super("Failed to update " + entityType + " with ID " + id + ": " + detail);
    }
} 