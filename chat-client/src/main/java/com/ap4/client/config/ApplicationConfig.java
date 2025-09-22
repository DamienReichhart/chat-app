package com.ap4.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.services.SessionService;

/**
 * Central configuration class for the application.
 * Manages application-wide configuration settings and provides access to specific
 * configuration components.
 * 
 * This class follows the Facade pattern to provide a simplified interface to
 * a set of configuration interfaces in the application.
 */
public class ApplicationConfig {
    private static final Logger logger = LogManager.getLogger(ApplicationConfig.class);
    
    /**
     * Configuration for application middlewares.
     */
    private final MiddlewaresConfig middlewaresConfig;
    
    /**
     * Configuration for WebSocket connections.
     */
    private final WebSocketConfig webSocketConfig;
    
    /**
     * Configuration for database connections.
     */
    private final DatabaseConfig databaseConfig;
    
    /**
     * Configuration for UI components.
     */
    private final UIConfig uiConfig;
    
    /**
     * General application properties.
     */
    private final Properties appProperties;

    /**
     * Creates a new ApplicationConfig instance with the provided session.
     * Initializes all necessary configuration components.
     * 
     * @param sessionService The application session instance to use for configuration
     */
    public ApplicationConfig(SessionService sessionService) {
        this.appProperties = loadApplicationProperties();
        this.middlewaresConfig = new MiddlewaresConfig(sessionService);
        this.webSocketConfig = new WebSocketConfig(loadWebSocketProperties());
        this.databaseConfig = new DatabaseConfig(loadDatabaseProperties());
        this.uiConfig = new UIConfig();
        
        logger.info("Application configuration initialized");
    }
    
    /**
     * Loads general application properties from the properties file.
     * 
     * @return Properties object containing application settings
     */
    private Properties loadApplicationProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.debug("Loaded application properties");
            } else {
                logger.warn("application.properties file not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Failed to load application properties", e);
        }
        return properties;
    }
    
    /**
     * Loads WebSocket configuration properties.
     * 
     * @return Properties object containing WebSocket settings
     */
    private Properties loadWebSocketProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("websocket.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.debug("Loaded WebSocket properties");
            } else {
                logger.warn("websocket.properties file not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Failed to load WebSocket properties", e);
        }
        return properties;
    }
    
    /**
     * Loads database configuration properties.
     * 
     * @return Properties object containing database settings
     */
    private Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.debug("Loaded database properties");
            } else {
                logger.warn("database.properties file not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
        }
        return properties;
    }

    /**
     * Gets application property by key.
     * 
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value or default if not found
     */
    public String getProperty(String key, String defaultValue) {
        return appProperties.getProperty(key, defaultValue);
    }

    /**
     * Retrieves the middlewares configuration component.
     * 
     * @return The middlewares configuration instance
     */
    public MiddlewaresConfig getMiddlewaresConfig() {
        return this.middlewaresConfig;
    }
    
    /**
     * Retrieves the WebSocket configuration component.
     * 
     * @return The WebSocket configuration instance
     */
    public WebSocketConfig getWebSocketConfig() {
        return this.webSocketConfig;
    }
    
    /**
     * Retrieves the database configuration component.
     * 
     * @return The database configuration instance
     */
    public DatabaseConfig getDatabaseConfig() {
        return this.databaseConfig;
    }
    
    /**
     * Retrieves the UI configuration component.
     * 
     * @return The UI configuration instance
     */
    public UIConfig getUiConfig() {
        return this.uiConfig;
    }
}
