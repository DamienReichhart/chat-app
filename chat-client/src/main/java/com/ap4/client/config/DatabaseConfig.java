package com.ap4.client.config;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class for database connections.
 * Provides centralized access to database-related settings.
 */
public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    
    /**
     * Default database URL if not specified in properties.
     */
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/chatapp";
    
    /**
     * Default database username if not specified in properties.
     */
    private static final String DEFAULT_DB_USERNAME = "postgres";
    
    /**
     * Default database password if not specified in properties.
     */
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    
    /**
     * Default maximum number of connections in the connection pool.
     */
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    
    /**
     * Properties containing database connection settings.
     */
    private final Properties properties;
    
    /**
     * Creates a new DatabaseConfig with the specified properties.
     * 
     * @param properties Properties containing database settings
     */
    public DatabaseConfig(Properties properties) {
        this.properties = properties;
        logger.debug("Database configuration initialized");
    }
    
    /**
     * Gets the database connection URL.
     * 
     * @return The configured database URL or default if not specified
     */
    public String getDbUrl() {
        return properties.getProperty("db.url", DEFAULT_DB_URL);
    }
    
    /**
     * Gets the database username.
     * 
     * @return The configured database username or default if not specified
     */
    public String getDbUsername() {
        return properties.getProperty("db.username", DEFAULT_DB_USERNAME);
    }
    
    /**
     * Gets the database password.
     * 
     * @return The configured database password or default if not specified
     */
    public String getDbPassword() {
        return properties.getProperty("db.password", DEFAULT_DB_PASSWORD);
    }
    
    /**
     * Gets the maximum number of connections in the connection pool.
     * 
     * @return The configured maximum connections or default if not specified
     */
    public int getMaxConnections() {
        try {
            String maxConnsStr = properties.getProperty("db.max_connections");
            if (maxConnsStr != null) {
                return Integer.parseInt(maxConnsStr);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid max connections value in configuration, using default", e);
        }
        return DEFAULT_MAX_CONNECTIONS;
    }
    
    /**
     * Gets whether connection pooling is enabled.
     * 
     * @return True if connection pooling is enabled, false otherwise
     */
    public boolean isConnectionPoolingEnabled() {
        String poolingStr = properties.getProperty("db.pooling.enabled");
        return poolingStr == null || Boolean.parseBoolean(poolingStr);
    }
    
    /**
     * Gets the database driver class name.
     * 
     * @return The configured driver class name or PostgreSQL driver if not specified
     */
    public String getDriverClassName() {
        return properties.getProperty("db.driver", "org.postgresql.Driver");
    }
} 