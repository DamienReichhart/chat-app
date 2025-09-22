package com.ap4.client.exceptions.db;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when there are issues with database transactions.
 */
public class TransactionException extends DataAccessException {
    
    public TransactionException(String message) {
        super(message);
    }
    
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TransactionException(Throwable cause) {
        super("Database transaction failed", cause);
    }
    
    public TransactionException(String operation, TransactionPhase phase, Throwable cause) {
        super("Transaction failed during " + phase.name() + " phase of " + operation, cause);
    }
    
    /**
     * Enum representing different phases of a database transaction.
     */
    public enum TransactionPhase {
        /** Beginning a transaction */
        BEGIN,
        
        /** Executing operations within a transaction */
        EXECUTION,
        
        /** Committing a transaction */
        COMMIT,
        
        /** Rolling back a transaction */
        ROLLBACK
    }
} 