package com.ap4.server.services;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.ap4.server.interfaces.services.IChatSessionService;

/**
 * Service to manage user sessions and track chat participation
 */
@Service
public class ChatSessionService implements IChatSessionService {
    private static final Logger logger = LogManager.getLogger(ChatSessionService.class);
    
    // Maps chatId to a map of userId to sessionId
    private final Map<Integer, Map<Integer, String>> chatUsers = new ConcurrentHashMap<>();
    
    // Maps sessionId to userId
    private final Map<String, Integer> sessionUsers = new ConcurrentHashMap<>();
    
    // Maps sessionId to chatId
    private final Map<String, Integer> sessionChats = new ConcurrentHashMap<>();
    
    /**
     * Add a user to a chat room
     * @param chatId Chat ID
     * @param userId User ID
     * @param sessionId WebSocket session ID
     */
    @Override
    public void addUserToChat(int chatId, int userId, String sessionId) {
        // Add to chat mapping
        Map<Integer, String> usersInChat = chatUsers.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>());
        usersInChat.put(userId, sessionId);
        
        // Add to session mappings
        sessionUsers.put(sessionId, userId);
        sessionChats.put(sessionId, chatId);
        
        logger.info("User {} added to chat {} with session {}", userId, chatId, sessionId);
        logger.info("Chat {} now has {} participants", chatId, usersInChat.size());
    }
    
    /**
     * Remove a user from a chat room
     * @param chatId Chat ID
     * @param userId User ID
     */
    @Override
    public void removeUserFromChat(int chatId, int userId) {
        Map<Integer, String> usersInChat = chatUsers.get(chatId);
        if (usersInChat != null) {
            String sessionId = usersInChat.remove(userId);
            
            if (sessionId != null) {
                sessionUsers.remove(sessionId);
                sessionChats.remove(sessionId);
                
                logger.info("User {} removed from chat {}", userId, chatId);
                
                // Remove empty chat rooms
                if (usersInChat.isEmpty()) {
                    chatUsers.remove(chatId);
                    logger.info("Removed empty chat room: {}", chatId);
                } else {
                    logger.info("Chat {} now has {} participants", chatId, usersInChat.size());
                }
            }
        }
    }
    
    /**
     * Remove a session and associated user from all chats
     * @param sessionId WebSocket session ID
     */
    @Override
    public void removeSession(String sessionId) {
        Integer userId = sessionUsers.remove(sessionId);
        Integer chatId = sessionChats.remove(sessionId);
        
        if (userId != null && chatId != null) {
            Map<Integer, String> usersInChat = chatUsers.get(chatId);
            if (usersInChat != null) {
                usersInChat.remove(userId);
                
                logger.info("User {} removed from chat {} due to session disconnect", userId, chatId);
                
                // Remove empty chat rooms
                if (usersInChat.isEmpty()) {
                    chatUsers.remove(chatId);
                    logger.info("Removed empty chat room: {}", chatId);
                }
            }
        }
    }
    
    /**
     * Get all users in a chat
     * @param chatId Chat ID
     * @return Set of user IDs
     */
    @Override
    public Set<Integer> getUsersInChat(int chatId) {
        Map<Integer, String> usersInChat = chatUsers.get(chatId);
        return usersInChat != null ? usersInChat.keySet() : Collections.emptySet();
    }
    
    /**
     * Get the chat ID a session is connected to
     * @param sessionId WebSocket session ID
     * @return Chat ID or null if not found
     */
    @Override
    public Integer getChatForSession(String sessionId) {
        return sessionChats.get(sessionId);
    }
    
    /**
     * Get the user ID associated with a session
     * @param sessionId WebSocket session ID
     * @return User ID or null if not found
     */
    @Override
    public Integer getUserForSession(String sessionId) {
        return sessionUsers.get(sessionId);
    }
} 