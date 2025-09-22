package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when there are issues executing a database query.
 */
public class QueryExecutionException extends DataAccessException {
    
    public QueryExecutionException(String message) {
        super(message);
    }
    
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public QueryExecutionException(Throwable cause) {
        super("Failed to execute database query", cause);
    }
    
    public QueryExecutionException(String queryDescription, String errorDetail, Throwable cause) {
        super("Failed to execute " + queryDescription + ": " + errorDetail, cause);
    }
} 