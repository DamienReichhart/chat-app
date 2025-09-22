package com.ap4.client.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.db.PostgresManager;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.db.QueryExecutionException;
import com.ap4.client.exceptions.db.TransactionException;
import com.ap4.client.interfaces.db.IDatabaseManager;
import com.ap4.client.interfaces.db.dao.IDAO;
import com.ap4.client.exceptions.security.AuthorizationException;

/**
 * Abstract base class for all DAO implementations.
 * Provides common functionality for database operations, including asynchronous support.
 * 
 * @param <T> The entity type this DAO handles
 * @param <K> The primary key type of the entity
 */
public abstract class AbstractDAO<T, K> implements IDAO<T, K> {
    
    protected static final Logger logger = LogManager.getLogger(AbstractDAO.class);
    
    /**
     * The database manager providing connections.
     */
    protected final IDatabaseManager dbManager;
    
    /**
     * The name of the table this DAO manages.
     */
    protected final String tableName;
    
    /**
     * Creates a new AbstractDAO with the specified table name.
     * 
     * @param tableName The name of the database table
     */
    public AbstractDAO(String tableName) {
        this.dbManager = PostgresManager.getInstance();
        this.tableName = tableName;
    }
    
    /**
     * Creates a new AbstractDAO with the specified database manager and table name.
     * Useful for testing with mock database managers.
     * 
     * @param dbManager The database manager to use
     * @param tableName The name of the database table
     */
    protected AbstractDAO(IDatabaseManager dbManager, String tableName) {
        this.dbManager = dbManager;
        this.tableName = tableName;
    }
    
    /**
     * Prepares a SQL statement with the given query and parameters.
     * Automatically determines parameter types (integer or string).
     * 
     * @param connection The database connection to use
     * @param query The SQL query
     * @param values The parameter values
     * @return The prepared statement
     * @throws SQLException if a database error occurs
     */
    protected PreparedStatement prepare(Connection connection, String query, Object... values) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                statement.setNull(i + 1, java.sql.Types.NULL);
                continue;
            }
            
            logger.trace("Setting parameter {} to {}", (i + 1), values[i]);
            
            if (values[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) values[i]);
            } else if (values[i] instanceof Long) {
                statement.setLong(i + 1, (Long) values[i]);
            } else if (values[i] instanceof Double) {
                statement.setDouble(i + 1, (Double) values[i]);
            } else if (values[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) values[i]);
            } else if (values[i] instanceof java.util.Date) {
                statement.setTimestamp(i + 1, 
                    new java.sql.Timestamp(((java.util.Date) values[i]).getTime()));
            } else if (values[i] instanceof Enum<?>) {
                statement.setString(i + 1, ((Enum<?>) values[i]).name());
            } else {
                statement.setString(i + 1, values[i].toString());
            }
        }
        return statement;
    }
    
    /**
     * Invalidates all entries in this DAO's cache.
     * Now this is a no-op since caching is removed.
     */
    @Override
    public void invalidateCache() {
        // No-op since caching is removed
        logger.debug("Cache invalidation requested but caching is disabled");
    }
    
    /**
     * Executes a database operation with proper connection handling.
     * Gets a connection, executes the operation, and handles any database errors.
     * 
     * @param <R> Return type of the operation
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws SQLException if a database error occurs
     */
    protected <R> R executeWithConnection(ConnectionOperation<R> operation) throws SQLException {
        Connection connection = null;
        try {
            connection = dbManager.getConnection();
            return operation.execute(connection);
        } catch (SQLException e) {
            logger.error("Error executing database operation", e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            // Map SQL exceptions to custom exceptions based on error messages
            if (errorMessage.contains("connect") || errorMessage.contains("timeout")) {
                throw new DatabaseConnectionException("database operation", e);
            } else if (errorMessage.contains("transaction")) {
                throw new TransactionException("database operation", e);
            } else if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                throw new DuplicateKeyException("entity", "field", errorMessage, e);
            } else if (errorMessage.contains("foreign key")) {
                throw new EntityRelationshipException("Operation failed due to entity relationship constraint", e);
            } else if (errorMessage.contains("permission") || errorMessage.contains("access")) {
                throw new AuthorizationException("Database operation", "Insufficient database permissions");
            } else {
                throw new QueryExecutionException("database operation", errorMessage, e);
            }
        }
    }
    
    /**
     * Executes a database operation asynchronously with proper connection handling.
     * Gets a connection asynchronously, executes the operation, and handles any errors.
     * 
     * @param <R> Return type of the operation
     * @param operation The operation to execute
     * @return A CompletableFuture that resolves to the result of the operation
     */
    protected <R> CompletableFuture<R> executeWithConnectionAsync(ConnectionOperation<R> operation) {
        return dbManager.executeAsync(connection -> {
            try {
                return operation.execute(connection);
            } catch (SQLException e) {
                logger.error("Error executing async database operation", e);
                String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // Map SQL exceptions to custom exceptions based on error messages
                if (errorMessage.contains("connect") || errorMessage.contains("timeout")) {
                    throw new DatabaseConnectionException("async database operation", e);
                } else if (errorMessage.contains("transaction")) {
                    throw new TransactionException("async database operation", e);
                } else if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                    throw new DuplicateKeyException("entity", "field", errorMessage, e);
                } else if (errorMessage.contains("foreign key")) {
                    throw new EntityRelationshipException("Operation failed due to entity relationship constraint", e);
                } else if (errorMessage.contains("permission") || errorMessage.contains("access")) {
                    throw new AuthorizationException("Database operation", "Insufficient database permissions");
                } else {
                    throw new QueryExecutionException("async database operation", errorMessage, e);
                }
            }
        });
    }
    
    /**
     * Executes a database operation asynchronously using the Function interface.
     * This method can be used when the operation doesn't throw SQLException.
     * 
     * @param <R> Return type of the operation
     * @param operation The operation to execute
     * @return A CompletableFuture that resolves to the result of the operation
     */
    protected <R> CompletableFuture<R> executeAsync(Function<Connection, R> operation) {
        return dbManager.executeAsync(operation);
    }
    
    /**
     * Functional interface for database operations that require a connection.
     * 
     * @param <R> Return type of the operation
     */
    @FunctionalInterface
    protected interface ConnectionOperation<R> {
        /**
         * Executes the operation with the given connection.
         * 
         * @param connection The database connection
         * @return The result of the operation
         * @throws SQLException if a database error occurs
         */
        R execute(Connection connection) throws SQLException;
    }
}
