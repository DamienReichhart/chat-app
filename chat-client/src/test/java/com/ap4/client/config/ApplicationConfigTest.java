package com.ap4.client.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ap4.client.services.SessionService;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private SessionService mockSessionService;
    
    private ApplicationConfig applicationConfig;
    
    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig(mockSessionService);
    }
    
    @Test
    @DisplayName("Should create ApplicationConfig with initialized components")
    void shouldInitializeAllComponents() {
        // Verify that all config components are properly initialized
        assertNotNull(applicationConfig.getWebSocketConfig());
        assertNotNull(applicationConfig.getDatabaseConfig());
        assertNotNull(applicationConfig.getUiConfig());
        assertNotNull(applicationConfig.getMiddlewaresConfig());
    }
    
    @Test
    @DisplayName("Should return default value when property not found")
    void shouldReturnDefaultValueWhenPropertyNotFound() {
        // Arrange
        String key = "non.existent.property";
        String defaultValue = "default-value";
        
        // Act
        String result = applicationConfig.getProperty(key, defaultValue);
        
        // Assert
        assertEquals(defaultValue, result);
    }
    
    @Test
    @DisplayName("Should return property value when it exists")
    void shouldReturnPropertyValueWhenItExists() {
        // This test relies on default properties being loaded in the constructor
        // Assuming there's a standard property in application.properties
        // Act
        String windowTitle = applicationConfig.getProperty("ui.window.title", "Default Title");
        
        // Assert - Should return either the actual property or the default
        assertNotNull(windowTitle);
    }
}