package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaseController abstract class
 */
@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private TestBaseController baseController;
    private WebSocketMessageDTO validMessage;
    private WebSocketMessageDTO messageWithoutUser;
    private WebSocketMessageDTO messageWithoutContent;

    @BeforeEach
    void setUp() {
        baseController = new TestBaseController(messagingTemplate);
        
        // Setup a valid message
        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        Message message = new Message();
        message.setContent("Test message content");
        message.setTimestamp(new Date());

        validMessage = new WebSocketMessageDTO();
        validMessage.setUser(user);
        validMessage.setMessage(message);
        validMessage.setChatId(1);
        
        // Setup message without user
        messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setMessage(new Message());
        messageWithoutUser.setChatId(1);
        
        // Setup message without content
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        
        messageWithoutContent = new WebSocketMessageDTO();
        messageWithoutContent.setUser(user2);
        messageWithoutContent.setChatId(1);
    }

    @Test
    @DisplayName("Create error message should set error field and add error message to content")
    void createErrorMessageShouldSetErrorFieldAndAddToContent() {
        // Arrange
        String errorMessage = "Test error message";
        
        // Act
        WebSocketMessageDTO result = baseController.testCreateErrorMessage(errorMessage);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(errorMessage, result.getError(), "Error field should match");
        assertEquals(errorMessage, result.getMessage().getContent(), "Message content should match error");
    }

    @Test
    @DisplayName("Validate message with valid data should return null")
    void validateMessageWithValidDataShouldReturnNull() {
        // Act
        WebSocketMessageDTO result = baseController.testValidateMessage(validMessage, 1, true);
        
        // Assert
        assertNull(result, "Should return null for valid message");
    }

    @Test
    @DisplayName("Validate message without user should return error")
    void validateMessageWithoutUserShouldReturnError() {
        // Act
        WebSocketMessageDTO result = baseController.testValidateMessage(messageWithoutUser, 1, true);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");
    }

    @Test
    @DisplayName("Validate message without content when required should return error")
    void validateMessageWithoutContentWhenRequiredShouldReturnError() {
        // Act
        WebSocketMessageDTO result = baseController.testValidateMessage(messageWithoutContent, 1, true);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("Message content missing"), 
                "Error should indicate missing message content");
    }

    @Test
    @DisplayName("Validate message without content when not required should return null")
    void validateMessageWithoutContentWhenNotRequiredShouldReturnNull() {
        // Act
        WebSocketMessageDTO result = baseController.testValidateMessage(messageWithoutContent, 1, false);
        
        // Assert
        assertNull(result, "Should return null when message not required");
    }
    
    /**
     * Concrete subclass of BaseController for testing
     */
    private static class TestBaseController extends BaseController {
        
        TestBaseController(SimpMessagingTemplate messagingTemplate) {
            super(messagingTemplate);
        }
        
        public WebSocketMessageDTO testCreateErrorMessage(String errorMessage) {
            return createErrorMessage(errorMessage);
        }
        
        public WebSocketMessageDTO testValidateMessage(WebSocketMessageDTO message, int chatId, boolean requireMessage) {
            return validateMessage(message, chatId, requireMessage);
        }
    }
} 