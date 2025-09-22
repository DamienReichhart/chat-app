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
 * Unit tests for PinController
 */
@ExtendWith(MockitoExtension.class)
class PinControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PinController pinController;

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
        message.setContent("Test message content");
        message.setTimestamp(new Date());
        message.setPinned(false);

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
    @DisplayName("Pin message with valid data should set pinned to true and return the message")
    void pinMessageWithValidDataShouldSetPinnedToTrueAndReturnMessage() {
        // Act
        WebSocketMessageDTO result = pinController.pinMessage(1, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNull(result.getError(), "No error should be set");
        assertTrue(result.getMessage().isPinned(), "Message should be pinned");
    }

    @Test
    @DisplayName("Pin message with missing user should return error")
    void pinMessageWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setMessage(validMessage.getMessage());
        messageWithoutUser.setChatId(1);

        // Act
        WebSocketMessageDTO result = pinController.pinMessage(1, messageWithoutUser);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");
    }

    @Test
    @DisplayName("Pin message with missing message content should return error")
    void pinMessageWithMissingMessageContentShouldReturnError() {
        // Act
        WebSocketMessageDTO result = pinController.pinMessage(1, invalidMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("Message content missing"), 
                "Error should indicate missing message content");
    }

    @Test
    @DisplayName("Unpin message with valid data should set pinned to false and return the message")
    void unpinMessageWithValidDataShouldSetPinnedToFalseAndReturnMessage() {
        // Arrange
        validMessage.getMessage().setPinned(true);
        
        // Act
        WebSocketMessageDTO result = pinController.unpinMessage(1, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNull(result.getError(), "No error should be set");
        assertFalse(result.getMessage().isPinned(), "Message should be unpinned");
    }

    @Test
    @DisplayName("Unpin message with missing user should return error")
    void unpinMessageWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setMessage(validMessage.getMessage());
        messageWithoutUser.setChatId(1);

        // Act
        WebSocketMessageDTO result = pinController.unpinMessage(1, messageWithoutUser);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");
    }

    @Test
    @DisplayName("Unpin message with missing message content should return error")
    void unpinMessageWithMissingMessageContentShouldReturnError() {
        // Act
        WebSocketMessageDTO result = pinController.unpinMessage(1, invalidMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("Message content missing"), 
                "Error should indicate missing message content");
    }
} 