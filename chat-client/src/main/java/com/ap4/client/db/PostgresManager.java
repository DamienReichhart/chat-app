package com.ap4.client.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.ap4.client.interfaces.db.IDatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Add imports for custom exceptions
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.QueryExecutionException;
import com.ap4.client.exceptions.service.ServiceUnavailableException;

/**
 * Database connection manager for PostgreSQL database.
 * Implements the IDatabaseManager interface and follows the Singleton pattern
 * to ensure only one database connection manager is maintained throughout the application lifecycle.
 */
public class PostgresManager implements IDatabaseManager {
    /**
     * Logger instance for this class.
     */
    private static final Logger logger = LogManager.getLogger(PostgresManager.class);
    
    /**
     * 
     * To load database.properties
     */
    private final Properties databaseProperties = new Properties();

    /**
     * Database connection properties from database.properties
     */
    private String DB_URL;
    private String DB_USERNAME;
    private String DB_PASSWORD;
    
    /**
     * The singleton instance of the PostgresManager.
     */
    private static PostgresManager instance;
    
    /**
     * The database connection.
     */
    private Connection connection;
    
    /**
     * Lock object for synchronizing connection operations
     */
    private final Object connectionLock = new Object();

    /**
     * Returns the singleton instance of the PostgresManager.
     * Creates a new instance if one doesn't exist yet.
     * 
     * @return The singleton PostgresManager instance
     */
    public static synchronized PostgresManager getInstance() {
        if (instance == null) {
            instance = new PostgresManager();
        }
        return instance;
    }

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the database connection.
     */
    private PostgresManager() {
        try {
            databaseProperties.load(PostgresManager.class.getClassLoader().getResourceAsStream("database.properties"));
            DB_URL = databaseProperties.getProperty("db.url");
            DB_USERNAME = databaseProperties.getProperty("db.username");
            DB_PASSWORD = databaseProperties.getProperty("db.password");
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            logger.info("PostgresManager initialized with a single database connection");
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw new DatabaseConnectionException("Failed to establish initial database connection", e);
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
            throw new ServiceUnavailableException("Database configuration", "Failed to load database properties", e);
        }
    }

    /**
     * Gets the database connection.
     * 
     * @return The database connection
     */
    @Override
    public Connection getConnection() {
        synchronized (connectionLock) {
            ensureConnectionIsValid();
            if (connection == null) {
                throw new DatabaseConnectionException("Database connection is not initialized");
            }
            return connection;
        }
    }
    
    /**
     * Asynchronously gets the database connection.
     * 
     * @return A CompletableFuture that resolves to the database connection
     */
    @Override
    public CompletableFuture<Connection> getConnectionAsync() {
        return CompletableFuture.supplyAsync(this::getConnection);
    }
    
    /**
     * Executes a database operation asynchronously.
     * Gets the connection, executes the operation, and returns the result.
     * 
     * @param <R> The return type of the database operation
     * @param operation The operation to execute with the connection
     * @return A CompletableFuture that resolves to the result of the operation
     */
    @Override
    public <R> CompletableFuture<R> executeAsync(Function<Connection, R> operation) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (connectionLock) {
                ensureConnectionIsValid();
                try {
                    return operation.apply(connection);
                } catch (Exception e) {
                    logger.error("Error executing database operation", e);
                    if (e instanceof DataAccessException) {
                        throw e;
                    } else if (e.getMessage() != null && e.getMessage().contains("query")) {
                        throw new QueryExecutionException("async database operation", e.getMessage(), e);
                    } else {
                        throw new DatabaseConnectionException("Error executing database operation", e);
                    }
                }
            }
        });
    }
    
    /**
     * Closes the database connection.
     */
    @Override
    public void closeAllConnections() {
        synchronized (connectionLock) {
            if (connection != null) {
                try {
                    connection.close();
                    logger.info("Database connection closed");
                } catch (SQLException e) {
                    logger.error("Error closing database connection", e);
                } finally {
                    connection = null; // Set to null to indicate that the connection is closed
                }
            }
        }
    }

    /**
     * Ensure that the connection is valid, if not, recreate a new instance of the connection
     */
    @Override
    public void ensureConnectionIsValid() {
        synchronized (connectionLock) {
            try {
                // Check if the connection is null or not valid
                if (connection == null || !connection.isValid(2)) {
                    logger.warn("Database connection is not valid. Attempting to create a new connection.");
                    // Close the existing connection if it's not null
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            logger.error("Error closing invalid connection", e);
                            // Continue despite error
                        }
                    }
                    // Create a new connection
                    connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                    logger.info("New database connection established.");
                }
            } catch (SQLException e) {
                logger.error("Failed to validate or establish database connection", e);
                throw new DatabaseConnectionException("Failed to validate or establish database connection", e);
            }
        }
    }
    
    /**
     * Finalize method to ensure the connection is closed if the instance is garbage collected.
     * This is a safety mechanism but should not be relied upon for resource cleanup.
     */
    @Override
    protected void finalize() {
        closeAllConnections();
    }
}