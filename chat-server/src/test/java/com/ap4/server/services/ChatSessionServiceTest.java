package com.ap4.server.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatSessionService
 */
public class ChatSessionServiceTest {
    
    private ChatSessionService chatSessionService;
    
    @BeforeEach
    void setUp() {
        chatSessionService = new ChatSessionService();
    }
    
    @Test
    @DisplayName("Add user to chat should store user-chat mapping correctly")
    void addUserToChatShouldStoreMapping() {
        // Arrange
        int chatId = 1;
        int userId = 123;
        String sessionId = "test-session-id";
        
        // Act
        chatSessionService.addUserToChat(chatId, userId, sessionId);
        
        // Assert
        Set<Integer> usersInChat = chatSessionService.getUsersInChat(chatId);
        assertNotNull(usersInChat, "Users in chat should not be null");
        assertTrue(usersInChat.contains(userId), "User should be in the chat");
        assertEquals(1, usersInChat.size(), "There should be exactly one user in the chat");
        
        assertEquals(chatId, chatSessionService.getChatForSession(sessionId), "Chat ID should match for session");
        assertEquals(userId, chatSessionService.getUserForSession(sessionId), "User ID should match for session");
    }
    
    @Test
    @DisplayName("Add multiple users to chat should store all mappings correctly")
    void addMultipleUsersToChatShouldStoreAllMappings() {
        // Arrange
        int chatId = 1;
        int userId1 = 123;
        int userId2 = 456;
        String sessionId1 = "test-session-id-1";
        String sessionId2 = "test-session-id-2";
        
        // Act
        chatSessionService.addUserToChat(chatId, userId1, sessionId1);
        chatSessionService.addUserToChat(chatId, userId2, sessionId2);
        
        // Assert
        Set<Integer> usersInChat = chatSessionService.getUsersInChat(chatId);
        assertNotNull(usersInChat, "Users in chat should not be null");
        assertTrue(usersInChat.contains(userId1), "User 1 should be in the chat");
        assertTrue(usersInChat.contains(userId2), "User 2 should be in the chat");
        assertEquals(2, usersInChat.size(), "There should be exactly two users in the chat");
    }
    
    @Test
    @DisplayName("Remove user from chat should remove the mapping")
    void removeUserFromChatShouldRemoveMapping() {
        // Arrange
        int chatId = 1;
        int userId = 123;
        String sessionId = "test-session-id";
        
        chatSessionService.addUserToChat(chatId, userId, sessionId);
        
        // Act
        chatSessionService.removeUserFromChat(chatId, userId);
        
        // Assert
        Set<Integer> usersInChat = chatSessionService.getUsersInChat(chatId);
        assertNotNull(usersInChat, "Users in chat should not be null even if empty");
        assertFalse(usersInChat.contains(userId), "User should no longer be in the chat");
        assertTrue(usersInChat.isEmpty(), "Chat should have no users");
        
        assertNull(chatSessionService.getUserForSession(sessionId), "User ID should be null for removed session");
        assertNull(chatSessionService.getChatForSession(sessionId), "Chat ID should be null for removed session");
    }
    
    @Test
    @DisplayName("Remove session should remove user from chat")
    void removeSessionShouldRemoveUserFromChat() {
        // Arrange
        int chatId = 1;
        int userId = 123;
        String sessionId = "test-session-id";
        
        chatSessionService.addUserToChat(chatId, userId, sessionId);
        
        // Act
        chatSessionService.removeSession(sessionId);
        
        // Assert
        Set<Integer> usersInChat = chatSessionService.getUsersInChat(chatId);
        assertNotNull(usersInChat, "Users in chat should not be null even if empty");
        assertFalse(usersInChat.contains(userId), "User should no longer be in the chat");
        assertTrue(usersInChat.isEmpty(), "Chat should have no users");
        
        assertNull(chatSessionService.getUserForSession(sessionId), "User ID should be null for removed session");
        assertNull(chatSessionService.getChatForSession(sessionId), "Chat ID should be null for removed session");
    }
    
    @Test
    @DisplayName("Get users in non-existent chat should return empty set")
    void getUsersInNonExistentChatShouldReturnEmptySet() {
        // Act
        Set<Integer> usersInChat = chatSessionService.getUsersInChat(999);
        
        // Assert
        assertNotNull(usersInChat, "Users in chat should not be null even for non-existent chat");
        assertTrue(usersInChat.isEmpty(), "Non-existent chat should have no users");
    }
    
    @Test
    @DisplayName("Get user for non-existent session should return null")
    void getUserForNonExistentSessionShouldReturnNull() {
        // Act & Assert
        assertNull(chatSessionService.getUserForSession("non-existent-session"), 
                "User ID should be null for non-existent session");
    }
    
    @Test
    @DisplayName("Get chat for non-existent session should return null")
    void getChatForNonExistentSessionShouldReturnNull() {
        // Act & Assert
        assertNull(chatSessionService.getChatForSession("non-existent-session"), 
                "Chat ID should be null for non-existent session");
    }
} 