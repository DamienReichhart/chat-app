package com.ap4.client.interfaces.db;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Interface for database connection management.
 * Defines the contract for database connectivity operations,
 * including both synchronous and asynchronous methods.
 */
public interface IDatabaseManager {
    
    /**
     * Gets a connection from the connection pool.
     * 
     * @return A database connection
     */
    Connection getConnection();
    
    /**
     * Asynchronously gets a connection from the pool.
     * 
     * @return A CompletableFuture that resolves to a database connection
     */
    CompletableFuture<Connection> getConnectionAsync();
    
    /**
     * Executes a database operation asynchronously.
     * Gets a connection, executes the operation, and releases the connection.
     * 
     * @param <R> The return type of the database operation
     * @param operation The operation to execute with the connection
     * @return A CompletableFuture that resolves to the result of the operation
     */
    <R> CompletableFuture<R> executeAsync(Function<Connection, R> operation);
    
    /**
     * Closes all connections and releases resources.
     */
    void closeAllConnections();

    /**
     * Assert that the connection is valid
     */
    void ensureConnectionIsValid();
}
