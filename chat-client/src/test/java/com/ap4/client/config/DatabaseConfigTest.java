package com.ap4.client.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

    private Properties properties;
    private DatabaseConfig databaseConfig;
    
    @BeforeEach
    void setUp() {
        properties = new Properties();
        databaseConfig = new DatabaseConfig(properties);
    }
    
    @Test
    @DisplayName("Should return default database URL when not specified in properties")
    void shouldReturnDefaultDatabaseUrl() {
        // Act
        String url = databaseConfig.getDbUrl();
        
        // Assert
        assertEquals("jdbc:postgresql://localhost:5432/chatapp", url);
    }
    
    @Test
    @DisplayName("Should return configured database URL when specified in properties")
    void shouldReturnConfiguredDatabaseUrl() {
        // Arrange
        String configuredUrl = "jdbc:postgresql://test-host:5432/test-db";
        properties.setProperty("db.url", configuredUrl);
        
        // Act
        String url = databaseConfig.getDbUrl();
        
        // Assert
        assertEquals(configuredUrl, url);
    }
    
    @Test
    @DisplayName("Should return default username when not specified in properties")
    void shouldReturnDefaultUsername() {
        // Act
        String username = databaseConfig.getDbUsername();
        
        // Assert
        assertEquals("postgres", username);
    }
    
    @Test
    @DisplayName("Should return configured username when specified in properties")
    void shouldReturnConfiguredUsername() {
        // Arrange
        String configuredUsername = "test-user";
        properties.setProperty("db.username", configuredUsername);
        
        // Act
        String username = databaseConfig.getDbUsername();
        
        // Assert
        assertEquals(configuredUsername, username);
    }
    
    @Test
    @DisplayName("Should return default password when not specified in properties")
    void shouldReturnDefaultPassword() {
        // Act
        String password = databaseConfig.getDbPassword();
        
        // Assert
        assertEquals("postgres", password);
    }
    
    @Test
    @DisplayName("Should return configured password when specified in properties")
    void shouldReturnConfiguredPassword() {
        // Arrange
        String configuredPassword = "test-password";
        properties.setProperty("db.password", configuredPassword);
        
        // Act
        String password = databaseConfig.getDbPassword();
        
        // Assert
        assertEquals(configuredPassword, password);
    }
    
    @Test
    @DisplayName("Should return default max connections when not specified in properties")
    void shouldReturnDefaultMaxConnections() {
        // Act
        int maxConnections = databaseConfig.getMaxConnections();
        
        // Assert
        assertEquals(10, maxConnections);
    }
    
    @Test
    @DisplayName("Should return configured max connections when specified in properties")
    void shouldReturnConfiguredMaxConnections() {
        // Arrange
        int configuredMaxConnections = 20;
        properties.setProperty("db.max_connections", String.valueOf(configuredMaxConnections));
        
        // Act
        int maxConnections = databaseConfig.getMaxConnections();
        
        // Assert
        assertEquals(configuredMaxConnections, maxConnections);
    }
    
    @Test
    @DisplayName("Should return default value for max connections when invalid value is specified")
    void shouldReturnDefaultMaxConnectionsWhenInvalidValueSpecified() {
        // Arrange
        properties.setProperty("db.max_connections", "invalid-value");
        
        // Act
        int maxConnections = databaseConfig.getMaxConnections();
        
        // Assert
        assertEquals(10, maxConnections);
    }
    
    @Test
    @DisplayName("Should return true for connection pooling when not specified in properties")
    void shouldReturnTrueForConnectionPoolingWhenNotSpecified() {
        // Act
        boolean poolingEnabled = databaseConfig.isConnectionPoolingEnabled();
        
        // Assert
        assertTrue(poolingEnabled);
    }
    
    @Test
    @DisplayName("Should return the configured value for connection pooling")
    void shouldReturnConfiguredValueForConnectionPooling() {
        // Arrange
        properties.setProperty("db.pooling.enabled", "false");
        
        // Act
        boolean poolingEnabled = databaseConfig.isConnectionPoolingEnabled();
        
        // Assert
        assertFalse(poolingEnabled);
    }
    
    @Test
    @DisplayName("Should return default driver class name when not specified in properties")
    void shouldReturnDefaultDriverClassName() {
        // Act
        String driverClassName = databaseConfig.getDriverClassName();
        
        // Assert
        assertEquals("org.postgresql.Driver", driverClassName);
    }
    
    @Test
    @DisplayName("Should return configured driver class name when specified in properties")
    void shouldReturnConfiguredDriverClassName() {
        // Arrange
        String configuredDriverClassName = "com.mysql.jdbc.Driver";
        properties.setProperty("db.driver", configuredDriverClassName);
        
        // Act
        String driverClassName = databaseConfig.getDriverClassName();
        
        // Assert
        assertEquals(configuredDriverClassName, driverClassName);
    }
} 