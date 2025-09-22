package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.User;
import com.ap4.server.interfaces.services.IChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionController
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private IChatSessionService chatSessionService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private WebSocketMessageDTO validMessage;
    private Map<String, Object> sessionAttributes;
    private final int chatId = 1;
    private final int userId = 123;
    private final String sessionId = "test-session-id";
    private final String username = "testUser";

    @BeforeEach
    void setUp() {
        // Set up session attributes
        sessionAttributes = new HashMap<>();
        
        // Making all stubbing lenient to avoid UnnecessaryStubbingException
        lenient().when(headerAccessor.getSessionId()).thenReturn(sessionId);
        lenient().when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        // Set up valid message with user
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        validMessage = new WebSocketMessageDTO();
        validMessage.setUser(user);
        validMessage.setChatId(chatId);
    }

    @Test
    @DisplayName("Join chat with valid data should store session data and return the message")
    void joinChatWithValidDataShouldStoreSessionDataAndReturnMessage() {
        // Act
        WebSocketMessageDTO result = subscriptionController.joinChat(chatId, validMessage, headerAccessor);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(validMessage, result, "Should return the same message object");
        assertNull(result.getError(), "No error should be set");

        // Verify session attributes were set
        assertEquals(username, sessionAttributes.get("username"), "Username should be stored in session");
        assertEquals(userId, sessionAttributes.get("userId"), "User ID should be stored in session");
        assertEquals(chatId, sessionAttributes.get("chatId"), "Chat ID should be stored in session");

        // Verify service was called
        verify(chatSessionService).addUserToChat(chatId, userId, sessionId);
    }

    @Test
    @DisplayName("Join chat with missing user should return error")
    void joinChatWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setChatId(chatId);

        // Act
        WebSocketMessageDTO result = subscriptionController.joinChat(chatId, messageWithoutUser, headerAccessor);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");

        // Verify service was not called
        verify(chatSessionService, never()).addUserToChat(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Leave chat with valid data should remove user and return the message")
    void leaveChatWithValidDataShouldRemoveUserAndReturnMessage() {
        // Act
        WebSocketMessageDTO result = subscriptionController.leaveChat(chatId, validMessage);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(validMessage, result, "Should return the same message object");
        assertNull(result.getError(), "No error should be set");

        // Verify service was called
        verify(chatSessionService).removeUserFromChat(chatId, userId);
    }

    @Test
    @DisplayName("Leave chat with missing user should return error")
    void leaveChatWithMissingUserShouldReturnError() {
        // Arrange
        WebSocketMessageDTO messageWithoutUser = new WebSocketMessageDTO();
        messageWithoutUser.setChatId(chatId);

        // Act
        WebSocketMessageDTO result = subscriptionController.leaveChat(chatId, messageWithoutUser);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getError(), "Error should be set");
        assertTrue(result.getError().contains("User information missing"), 
                "Error should indicate missing user information");

        // Verify service was not called
        verify(chatSessionService, never()).removeUserFromChat(anyInt(), anyInt());
    }
} 