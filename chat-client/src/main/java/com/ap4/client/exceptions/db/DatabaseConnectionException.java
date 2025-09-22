package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when there are issues connecting to the database.
 */
public class DatabaseConnectionException extends DataAccessException {
    
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseConnectionException(Throwable cause) {
        super("Failed to connect to the database", cause);
    }
    
    /**
     * Constructor with operation name that describes what was being attempted
     * when the connection failed.
     * 
     * @param operationName The name of the database operation that was being attempted
     * @param cause The underlying cause of the connection failure
     */
    public DatabaseConnectionException(String operationName, Throwable cause, boolean isOperation) {
        super("Database connection failed during " + operationName, cause);
    }
} 