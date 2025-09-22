package com.ap4.server.interfaces.services;

import java.util.Set;

/**
 * Interface for services that manage chat sessions
 */
public interface IChatSessionService {
    
    /**
     * Add a user to a chat room
     * @param chatId Chat ID
     * @param userId User ID
     * @param sessionId WebSocket session ID
     */
    void addUserToChat(int chatId, int userId, String sessionId);
    
    /**
     * Remove a user from a chat room
     * @param chatId Chat ID
     * @param userId User ID
     */
    void removeUserFromChat(int chatId, int userId);
    
    /**
     * Remove a session and associated user from all chats
     * @param sessionId WebSocket session ID
     */
    void removeSession(String sessionId);
    
    /**
     * Get all users in a chat
     * @param chatId Chat ID
     * @return Set of user IDs
     */
    Set<Integer> getUsersInChat(int chatId);
    
    /**
     * Get the chat ID a session is connected to
     * @param sessionId WebSocket session ID
     * @return Chat ID or null if not found
     */
    Integer getChatForSession(String sessionId);
    
    /**
     * Get the user ID associated with a session
     * @param sessionId WebSocket session ID
     * @return User ID or null if not found
     */
    Integer getUserForSession(String sessionId);
} 