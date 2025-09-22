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
 * Unit tests for MessageController
 */
@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageController messageController;

    private WebSocketMessageDTO validMessage;
    private WebSocketMessageDTO invalidMessage;

    @BeforeEach
    void setUp() {
        // Set up a valid message
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
    @DisplayName("Send chat message with valid data should return the same message")
    void sendChatMessageWithValidDataShouldReturnSameMessage() {
        // Act
        WebSocketMessageDTO result = messageController.sendChatMessage(1, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(validMessage, result, "Should return the same message object");
        assertNull(result.getError(), "No error should be set");
    }

    @Test
    @DisplayName("Send chat message with missing user should return error")
    void sendChatMessageWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setMessage(validMessage.getMessage());
        messageWithoutUser.setChatId(1);

        // Act
        WebSocketMessageDTO result = messageController.sendChatMessage(1, messageWithoutUser);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");
    }

    @Test
    @DisplayName("Send chat message with missing message content should return error")
    void sendChatMessageWithMissingMessageContentShouldReturnError() {
        // Act
        WebSocketMessageDTO result = messageController.sendChatMessage(1, invalidMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("Message content missing"), 
                "Error should indicate missing message content");
    }

    @Test
    @DisplayName("Send chat message with chat ID mismatch should still work")
    void sendChatMessageWithChatIdMismatchShouldStillWork() {
        // Arrange
        int requestedChatId = 2; // Different from the message's chatId (1)

        // Act
        WebSocketMessageDTO result = messageController.sendChatMessage(requestedChatId, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(validMessage, result, "Should return the same message object");
        assertNull(result.getError(), "No error should be set");
    }
} 