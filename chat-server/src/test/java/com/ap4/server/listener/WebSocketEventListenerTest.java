package com.ap4.server.listener;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.server.interfaces.services.IChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketEventListener
 */
@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private IChatSessionService chatSessionService;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<WebSocketMessageDTO> messageCaptor;

    private Message<byte[]> message;
    private StompHeaderAccessor headerAccessor;
    private final String sessionId = "test-session-id";
    private final String username = "testUser";
    private final int userId = 123;
    private final int chatId = 1;

    @BeforeEach
    void setUp() {
        // Set up header accessor for the disconnect event
        headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        
        // Create a message with headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("stompCommand", StompCommand.DISCONNECT);
        headers.put(StompHeaderAccessor.SESSION_ID_HEADER, sessionId);
        message = new GenericMessage<>(new byte[0], headers);
    }

    @Test
    @DisplayName("Handle WebSocket disconnect event with no session attributes should not send leave message")
    void handleWebSocketDisconnectListenerWithNoSessionAttributesShouldNotSendLeaveMessage() {
        // Arrange
        when(disconnectEvent.getMessage()).thenReturn(message);
        Map<String, Object> sessionAttributes = null;
        headerAccessor.setSessionAttributes(sessionAttributes);
        
        // Act
        webSocketEventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(WebSocketMessageDTO.class));
        verify(chatSessionService, never()).removeUserFromChat(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Handle WebSocket disconnect event with incomplete session should not send message")
    void handleWebSocketDisconnectListenerWithIncompleteSessionShouldNotSendMessage() {
        // Arrange
        when(disconnectEvent.getMessage()).thenReturn(message);
        
        // Set up session attributes with incomplete user info
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("username", username);
        // Missing userId and chatId
        headerAccessor.setSessionAttributes(sessionAttributes);
        
        // Act
        webSocketEventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Assert
        verify(chatSessionService, never()).removeUserFromChat(anyInt(), anyInt());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(WebSocketMessageDTO.class));
    }
    
    @Test
    @DisplayName("Handle WebSocket disconnect event with complete session should send leave message")
    void handleWebSocketDisconnectListenerWithCompleteSessionShouldSendLeaveMessage() {
        // Arrange
        when(disconnectEvent.getMessage()).thenReturn(message);
        
        // Set up session attributes with complete user info
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("username", username);
        sessionAttributes.put("userId", userId);
        sessionAttributes.put("chatId", chatId);
        headerAccessor.setSessionAttributes(sessionAttributes);
        
        // Configure mock for StompHeaderAccessor.wrap to return our configured headerAccessor
        try (var headerAccessorMock = mockStatic(StompHeaderAccessor.class)) {
            headerAccessorMock.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);
            
            // Act
            webSocketEventListener.handleWebSocketDisconnectListener(disconnectEvent);
            
            // Assert
            verify(chatSessionService).removeUserFromChat(chatId, userId);
            
            // Verify message sending
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/chat/" + chatId + "/leave"), 
                    messageCaptor.capture());
            
            // Verify the leave message content
            WebSocketMessageDTO leaveMessage = messageCaptor.getValue();
            assertNotNull(leaveMessage, "Leave message should not be null");
            assertNotNull(leaveMessage.getUser(), "User in leave message should not be null");
            assertEquals(userId, leaveMessage.getUser().getId(), "User ID should match");
            assertEquals(username, leaveMessage.getUser().getUsername(), "Username should match");
            assertEquals(chatId, leaveMessage.getChatId(), "Chat ID should match");
        }
    }
} 