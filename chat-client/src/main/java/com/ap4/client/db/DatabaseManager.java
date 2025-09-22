package com.ap4.client.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.config.DatabaseConfig;

/**
 * Database connection manager for the application.
 * Handles creating, caching, and closing database connections.
 * Implements the Singleton pattern to ensure a single instance throughout the application.
 */
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    
    /**
     * Singleton instance of the DatabaseManager.
     */
    private static DatabaseManager instance;
    
    /**
     * Database configuration.
     */
    private final DatabaseConfig databaseConfig;
    
    /**
     * Map of active connections.
     */
    private final ConcurrentHashMap<String, Connection> connections;
    
    /**
     * Gets the singleton instance of the DatabaseManager.
     * 
     * @param databaseConfig The database configuration to use
     * @return The singleton instance
     */
    public static synchronized DatabaseManager getInstance(DatabaseConfig databaseConfig) {
        if (instance == null) {
            instance = new DatabaseManager(databaseConfig);
        }
        return instance;
    }
    
    /**
     * Gets the singleton instance of the DatabaseManager.
     * 
     * @return The singleton instance
     * @throws IllegalStateException if the manager hasn't been initialized with a configuration
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager has not been initialized with a configuration");
        }
        return instance;
    }
    
    /**
     * Creates a new DatabaseManager with the specified configuration.
     * Private constructor to enforce the Singleton pattern.
     * 
     * @param databaseConfig The database configuration to use
     */
    private DatabaseManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.connections = new ConcurrentHashMap<>();
        
        try {
            // Load the database driver
            Class.forName(databaseConfig.getDriverClassName());
            logger.info("Database driver loaded: {}", databaseConfig.getDriverClassName());
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load database driver", e);
        }
    }
    
    /**
     * Gets a database connection.
     * If a connection with the specified name exists and is valid, it will be returned.
     * Otherwise, a new connection will be created.
     * 
     * @param connectionName Name to identify the connection
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection(String connectionName) throws SQLException {
        // Check if we already have a valid connection with this name
        Connection existingConnection = connections.get(connectionName);
        if (existingConnection != null && !existingConnection.isClosed() && existingConnection.isValid(1)) {
            return existingConnection;
        }
        
        // Create a new connection
        Connection connection = DriverManager.getConnection(
            databaseConfig.getDbUrl(), 
            databaseConfig.getDbUsername(), 
            databaseConfig.getDbPassword()
        );
        
        // Configure connection
        connection.setAutoCommit(true);
        
        // Store the connection
        connections.put(connectionName, connection);
        logger.debug("Created new database connection: {}", connectionName);
        
        return connection;
    }
    
    /**
     * Gets a database connection with the default name.
     * 
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return getConnection("default");
    }
    
    /**
     * Closes a specific connection.
     * 
     * @param connectionName The name of the connection to close
     */
    public void closeConnection(String connectionName) {
        Connection connection = connections.remove(connectionName);
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.debug("Closed database connection: {}", connectionName);
                }
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", connectionName, e);
            }
        }
    }
    
    /**
     * Closes all active connections.
     */
    public void closeAllConnections() {
        List<String> connectionNames = new ArrayList<>(connections.keySet());
        for (String connectionName : connectionNames) {
            closeConnection(connectionName);
        }
        logger.info("All database connections closed");
    }
    
    /**
     * Test the database connection.
     * 
     * @return true if the connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection connection = DriverManager.getConnection(
                databaseConfig.getDbUrl(), 
                databaseConfig.getDbUsername(), 
                databaseConfig.getDbPassword())) {
            
            boolean isValid = connection.isValid(3);
            if (isValid) {
                logger.info("Database connection test successful");
            } else {
                logger.warn("Database connection test failed: connection is not valid");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
} 