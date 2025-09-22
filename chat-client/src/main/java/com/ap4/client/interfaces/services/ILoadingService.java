package com.ap4.client.interfaces.services;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.ap4.common.models.User;

/**
 * Interface for the loading service that preloads user conversations and related data.
 * This service is responsible for loading user chats and messages asynchronously
 * and handling WebSocket connections for real-time messaging.
 */
public interface ILoadingService {
    
    /**
     * Load user data asynchronously and handle WebSocket connection.
     * This method loads the user, their chats, and messages in an optimized way
     * while providing progress updates.
     *
     * @param progressCallback Callback function to report progress, taking a status string and a progress value (0.0-1.0)
     * @param completionCallback Callback function executed when loading is completed or failed, takes a boolean success flag and an optional message
     */
    void loadUserData(BiConsumer<String, Double> progressCallback, BiConsumer<Boolean, String> completionCallback);
    
    /**
     * Get the current user from the session or attempt to retrieve by session ID.
     * 
     * @param progressCallback Callback function to report progress status
     * @return The User object if found, otherwise null
     */
    User getCurrentUser(BiConsumer<String, Double> progressCallback);
    
    /**
     * Connect to the WebSocket server for real-time messaging.
     * 
     * @param user The user to connect with
     * @param progressCallback Callback function to report progress
     * @return True if connection was initiated successfully, false otherwise
     */
    boolean connectWebSocket(User user, BiConsumer<String, Double> progressCallback);
    
    /**
     * Asynchronously preload all chats and their messages for the authenticated user.
     * 
     * @param userId The ID of the authenticated user
     * @param progressCallback Callback function to report progress status and value
     * @return A CompletableFuture that completes when all chats and messages are loaded
     */
    CompletableFuture<Void> preloadUserChatsAsync(int userId, BiConsumer<String, Double> progressCallback);
} 