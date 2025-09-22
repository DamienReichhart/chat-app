package com.ap4.client.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebSocketConfigTest {

    private Properties properties;
    private WebSocketConfig webSocketConfig;
    
    @BeforeEach
    void setUp() {
        properties = new Properties();
        webSocketConfig = new WebSocketConfig(properties);
    }
    
    @Test
    @DisplayName("Should return default server URL when not specified in properties")
    void shouldReturnDefaultServerUrl() {
        // Act
        String serverUrl = webSocketConfig.getServerUrl();
        
        // Assert
        assertEquals("ws://localhost:8080/chat", serverUrl);
    }
    
    @Test
    @DisplayName("Should return configured server URL when specified in properties")
    void shouldReturnConfiguredServerUrl() {
        // Arrange
        String configuredUrl = "ws://test-server:9090/chat";
        properties.setProperty("websocket.url", configuredUrl);
        
        // Act
        String serverUrl = webSocketConfig.getServerUrl();
        
        // Assert
        assertEquals(configuredUrl, serverUrl);
    }
    
    @Test
    @DisplayName("Should return default connection timeout when not specified in properties")
    void shouldReturnDefaultConnectionTimeout() {
        // Act
        int connectionTimeout = webSocketConfig.getConnectionTimeout();
        
        // Assert
        assertEquals(5000, connectionTimeout);
    }
    
    @Test
    @DisplayName("Should return configured connection timeout when specified in properties")
    void shouldReturnConfiguredConnectionTimeout() {
        // Arrange
        int configuredTimeout = 10000;
        properties.setProperty("websocket.timeout", String.valueOf(configuredTimeout));
        
        // Act
        int connectionTimeout = webSocketConfig.getConnectionTimeout();
        
        // Assert
        assertEquals(configuredTimeout, connectionTimeout);
    }
    
    @Test
    @DisplayName("Should return default timeout when invalid value is specified")
    void shouldReturnDefaultTimeoutWhenInvalidValueSpecified() {
        // Arrange
        properties.setProperty("websocket.timeout", "invalid-value");
        
        // Act
        int connectionTimeout = webSocketConfig.getConnectionTimeout();
        
        // Assert
        assertEquals(5000, connectionTimeout);
    }
    
    @Test
    @DisplayName("Should return default reconnect attempts when not specified in properties")
    void shouldReturnDefaultReconnectAttempts() {
        // Act
        int reconnectAttempts = webSocketConfig.getReconnectAttempts();
        
        // Assert
        assertEquals(5, reconnectAttempts);
    }
    
    @Test
    @DisplayName("Should return configured reconnect attempts when specified in properties")
    void shouldReturnConfiguredReconnectAttempts() {
        // Arrange
        int configuredAttempts = 10;
        properties.setProperty("websocket.reconnect.attempts", String.valueOf(configuredAttempts));
        
        // Act
        int reconnectAttempts = webSocketConfig.getReconnectAttempts();
        
        // Assert
        assertEquals(configuredAttempts, reconnectAttempts);
    }
    
    @Test
    @DisplayName("Should return default reconnect attempts when invalid value is specified")
    void shouldReturnDefaultReconnectAttemptsWhenInvalidValueSpecified() {
        // Arrange
        properties.setProperty("websocket.reconnect.attempts", "invalid-value");
        
        // Act
        int reconnectAttempts = webSocketConfig.getReconnectAttempts();
        
        // Assert
        assertEquals(5, reconnectAttempts);
    }
    
    @Test
    @DisplayName("Should return false for auto reconnect when not specified in properties")
    void shouldReturnFalseForAutoReconnectWhenNotSpecified() {
        // Act
        boolean autoReconnect = webSocketConfig.isAutoReconnectEnabled();
        
        // Assert
        assertFalse(autoReconnect);
    }
    
    @Test
    @DisplayName("Should return the configured value for auto reconnect")
    void shouldReturnConfiguredValueForAutoReconnect() {
        // Arrange
        properties.setProperty("websocket.reconnect.auto", "true");
        
        // Act
        boolean autoReconnect = webSocketConfig.isAutoReconnectEnabled();
        
        // Assert
        assertTrue(autoReconnect);
    }
} 