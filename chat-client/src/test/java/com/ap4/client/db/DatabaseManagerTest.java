package com.ap4.client.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ap4.client.config.DatabaseConfig;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseManagerTest {

    @Mock
    private DatabaseConfig mockDatabaseConfig;
    
    @Mock
    private Connection mockConnection;
    
    private DatabaseManager databaseManager;
    private MockedStatic<DriverManager> mockedDriverManager;
    
    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException, ReflectiveOperationException {
        // Reset singleton instance
        resetDatabaseManagerInstance();
        
        // Configure mock database config
        lenient().when(mockDatabaseConfig.getDbUrl()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        lenient().when(mockDatabaseConfig.getDbUsername()).thenReturn("testuser");
        lenient().when(mockDatabaseConfig.getDbPassword()).thenReturn("testpass");
        lenient().when(mockDatabaseConfig.getDriverClassName()).thenReturn("org.postgresql.Driver");
        
        // Mock the driver manager to return our mock connection
        mockedDriverManager = Mockito.mockStatic(DriverManager.class);
        mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
            .thenReturn(mockConnection);
        
        // Mock connection state
        lenient().when(mockConnection.isClosed()).thenReturn(false);
        lenient().when(mockConnection.isValid(anyInt())).thenReturn(true);
        
        // Create the database manager instance with our mock config
        databaseManager = DatabaseManager.getInstance(mockDatabaseConfig);
    }
    
    /**
     * Resets the DatabaseManager singleton instance
     */
    private void resetDatabaseManagerInstance() throws ReflectiveOperationException {
        Field instanceField = DatabaseManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up static mock
        mockedDriverManager.close();
        
        // Close all connections
        databaseManager.closeAllConnections();
    }
    
    @Test
    @DisplayName("Should return the same instance when using getInstance with config")
    void shouldReturnSameInstanceWithConfig() {
        // Act
        DatabaseManager instance1 = DatabaseManager.getInstance(mockDatabaseConfig);
        DatabaseManager instance2 = DatabaseManager.getInstance(mockDatabaseConfig);
        
        // Assert
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should return the same instance when using getInstance without config")
    void shouldReturnSameInstanceWithoutConfig() {
        // Act
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        
        // Assert
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should create a new connection when one doesn't exist")
    void shouldCreateNewConnectionWhenNotExists() throws SQLException {
        // Act
        Connection connection = databaseManager.getConnection("testConnection");
        
        // Assert
        assertNotNull(connection);
        assertEquals(mockConnection, connection);
        
        // Verify
        verify(mockDatabaseConfig).getDbUrl();
        verify(mockDatabaseConfig).getDbUsername();
        verify(mockDatabaseConfig).getDbPassword();
    }
    
    @Test
    @DisplayName("Should reuse existing connection when it's valid")
    void shouldReuseExistingConnectionWhenValid() throws SQLException {
        // Arrange - get a connection first
        Connection firstConnection = databaseManager.getConnection("testConnection");
        
        // Reset invocation counts
        reset(mockDatabaseConfig);
        
        // Act - get the same connection again
        Connection secondConnection = databaseManager.getConnection("testConnection");
        
        // Assert
        assertSame(firstConnection, secondConnection);
        
        // Verify - should not attempt to get a new connection
        verify(mockDatabaseConfig, never()).getDbUrl();
        verify(mockDatabaseConfig, never()).getDbUsername();
        verify(mockDatabaseConfig, never()).getDbPassword();
    }
    
    @Test
    @DisplayName("Should create a new connection when existing one is closed")
    void shouldCreateNewConnectionWhenExistingIsClosed() throws SQLException {
        // Arrange - get a connection first
        databaseManager.getConnection("testConnection");
        
        // Make the connection appear closed for next call
        when(mockConnection.isClosed()).thenReturn(true);
        
        // Reset invocation counts
        reset(mockDatabaseConfig);
        lenient().when(mockDatabaseConfig.getDbUrl()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        lenient().when(mockDatabaseConfig.getDbUsername()).thenReturn("testuser");
        lenient().when(mockDatabaseConfig.getDbPassword()).thenReturn("testpass");
        
        // Act - get the same connection again
        Connection secondConnection = databaseManager.getConnection("testConnection");
        
        // Assert
        assertNotNull(secondConnection);
        
        // Verify - should attempt to get a new connection
        verify(mockDatabaseConfig).getDbUrl();
        verify(mockDatabaseConfig).getDbUsername();
        verify(mockDatabaseConfig).getDbPassword();
    }
    
    @Test
    @DisplayName("Should create a new connection when existing one is invalid")
    void shouldCreateNewConnectionWhenExistingIsInvalid() throws SQLException {
        // Arrange - get a connection first
        databaseManager.getConnection("testConnection");
        
        // Make the connection appear invalid for next call
        when(mockConnection.isValid(anyInt())).thenReturn(false);
        
        // Reset invocation counts
        reset(mockDatabaseConfig);
        lenient().when(mockDatabaseConfig.getDbUrl()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        lenient().when(mockDatabaseConfig.getDbUsername()).thenReturn("testuser");
        lenient().when(mockDatabaseConfig.getDbPassword()).thenReturn("testpass");
        
        // Act - get the same connection again
        Connection secondConnection = databaseManager.getConnection("testConnection");
        
        // Assert
        assertNotNull(secondConnection);
        
        // Verify - should attempt to get a new connection
        verify(mockDatabaseConfig).getDbUrl();
        verify(mockDatabaseConfig).getDbUsername();
        verify(mockDatabaseConfig).getDbPassword();
    }
    
    @Test
    @DisplayName("Should close connection when closeConnection is called")
    void shouldCloseConnectionWhenCloseConnectionCalled() throws SQLException {
        // Arrange - get a connection first
        databaseManager.getConnection("testConnection");
        
        // Act
        databaseManager.closeConnection("testConnection");
        
        // Verify
        verify(mockConnection).close();
    }
    
    @Test
    @DisplayName("Should close all connections when closeAllConnections is called")
    void shouldCloseAllConnectionsWhenCloseAllConnectionsCalled() throws SQLException {
        // Arrange - get multiple connections
        databaseManager.getConnection("connection1");
        databaseManager.getConnection("connection2");
        databaseManager.getConnection("connection3");
        
        // Act
        databaseManager.closeAllConnections();
        
        // Verify - close should be called for each connection
        verify(mockConnection, times(3)).close();
    }
    
    @Test
    @DisplayName("Should return true when test connection is successful")
    void shouldReturnTrueWhenTestConnectionIsSuccessful() throws SQLException {
        // Act
        boolean result = databaseManager.testConnection();
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should return false when test connection fails")
    void shouldReturnFalseWhenTestConnectionFails() throws SQLException {
        // Arrange
        when(mockConnection.isValid(anyInt())).thenReturn(false);
        
        // Act
        boolean result = databaseManager.testConnection();
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should handle SQLException when testing connection")
    void shouldHandleSQLExceptionWhenTestingConnection() throws SQLException {
        // Suppress database logger output temporarily
        org.apache.logging.log4j.core.Logger loggerToSuppress = 
            (org.apache.logging.log4j.core.Logger) LogManager.getLogger(DatabaseManager.class);
        Level originalLevel = loggerToSuppress.getLevel();
        loggerToSuppress.setLevel(Level.OFF);
        
        try {
            // Your existing test code here
            SQLException sqlException = new SQLException("Test exception");
            mockedDriverManager.reset();
            mockedDriverManager.when(() -> DriverManager.getConnection(
                anyString(), anyString(), anyString()
            )).thenThrow(sqlException);
            
            // Reset singleton instance to force creation of a new one
            resetDatabaseManagerInstance();
            
            // Act
            boolean result = DatabaseManager.getInstance(mockDatabaseConfig).testConnection();
            
            // Assert
            assertFalse(result);
        } catch (ReflectiveOperationException e) {} 
        finally {
            // Restore original logging level
            loggerToSuppress.setLevel(originalLevel);
        }
    }
} 