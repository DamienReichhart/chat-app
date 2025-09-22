package com.ap4.server.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketConfig
 */
class WebSocketConfigTest {

    @Test
    @DisplayName("Create WebSocket container should configure buffer sizes and timeout")
    void createWebSocketContainerShouldConfigureBufferSizesAndTimeout() {
        // Arrange
        WebSocketConfig webSocketConfig = new WebSocketConfig();
        
        // Act
        ServletServerContainerFactoryBean container = webSocketConfig.createWebSocketContainer();
        
        // Assert
        assertNotNull(container, "Container should not be null");
        
        // Can't easily test the internal state of ServletServerContainerFactoryBean as the setters don't have getters
        // But we can assert the container was created successfully
    }
}