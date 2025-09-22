package com.ap4.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BootstrapTest {

    @Test
    @DisplayName("Bootstrap should call ChatClientApplication.main")
    void shouldCallChatClientApplicationMain() {
        // Arrange
        String[] args = {"arg1", "arg2"};
        
        try (MockedStatic<ChatClientApplication> mockStatic = mockStatic(ChatClientApplication.class)) {
            // Act
            Bootstrap.main(args);
            
            // Assert
            mockStatic.verify(() -> ChatClientApplication.main(args));
        }
    }
}