package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageManagementController
 */
@ExtendWith(MockitoExtension.class)
class MessageManagementControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageManagementController messageManagementController;

    private WebSocketMessageDTO validMessage;
    private WebSocketMessageDTO invalidMessage;

    @BeforeEach
    void setUp() {
        // Set up a valid message
        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        Message message = new Message();
        message.setId(100);
        message.setContent("Test message to delete");
        message.setTimestamp(new Date());

        validMessage = new WebSocketMessageDTO();
        validMessage.setUser(user);
        validMessage.setMessage(message);
        validMessage.setChatId(1);

        // Set up an invalid message (no message content)
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("testUser2");

        invalidMessage = new WebSocketMessageDTO();
        invalidMessage.setUser(user2);
        invalidMessage.setChatId(1);
        // No message object set
    }

    @Test
    @DisplayName("Delete message with valid data should return the same message")
    void deleteMessageWithValidDataShouldReturnSameMessage() {
        // Act
        WebSocketMessageDTO result = messageManagementController.deleteMessage(1, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(validMessage, result, "Should return the same message object");
        assertNull(result.getError(), "No error should be set");
    }

    @Test
    @DisplayName("Delete message with missing user should return error")
    void deleteMessageWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setMessage(validMessage.getMessage());
        messageWithoutUser.setChatId(1);

        // Act
        WebSocketMessageDTO result = messageManagementController.deleteMessage(1, messageWithoutUser);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");
    }

    @Test
    @DisplayName("Delete message with missing message content should return error")
    void deleteMessageWithMissingMessageContentShouldReturnError() {
        // Act
        WebSocketMessageDTO result = messageManagementController.deleteMessage(1, invalidMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("Message content missing"), 
                "Error should indicate missing message content");
    }
} 