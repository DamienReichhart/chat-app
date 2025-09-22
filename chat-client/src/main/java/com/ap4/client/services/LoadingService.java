package com.ap4.client.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.ILoadingService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.interfaces.websocket.ConnectionListener;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Implementation of the ILoadingService interface.
 * Handles asynchronous loading of user data, chats, and messages.
 */
public class LoadingService implements ILoadingService {
    private static final Logger logger = LogManager.getLogger(LoadingService.class);
    
    private final IChatService chatService;
    private final IMessageService messageService;
    private final IUserService userService;
    private final WebSocketService webSocketService;
    private final SessionService sessionService;
    
    /**
     * Default constructor that initializes required services.
     */
    public LoadingService() {
        this.chatService = new ChatService();
        this.messageService = new MessageService();
        this.userService = new UserService();
        this.webSocketService = WebSocketService.getInstance();
        this.sessionService = SessionService.getInstance();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public LoadingService(IChatService chatService, IMessageService messageService, 
                        IUserService userService, WebSocketService webSocketService,
                        SessionService sessionService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.sessionService = sessionService;
    }
    
    @Override
    public void loadUserData(BiConsumer<String, Double> progressCallback, BiConsumer<Boolean, String> completionCallback) {
        try {
            // Log current session state for debugging
            logger.debug("Session state when loading: {}", sessionService.toString());
            
            // Get current user
            User currentUser = getCurrentUser(progressCallback);
            
            // If no user, notify completion with failure
            if (currentUser == null) {
                logger.error("No user available, notifying completion with failure");
                progressCallback.accept("Error: User session not found", 0.0);
                completionCallback.accept(false, "No user session found");
                return;
            }

            // Connect to WebSocket server
            connectWebSocket(currentUser, progressCallback);

            // Start preloading in the background using async methods
            preloadUserChatsAsync(currentUser.getId(), progressCallback)
                .thenRun(() -> {
                    // Success - loading completed
                    completionCallback.accept(true, "Successfully loaded all data");
                })
                .exceptionally(ex -> {
                    // Handle failure
                    logger.error("Error during loading: {}", ex.getMessage(), ex);
                    completionCallback.accept(false, "Error loading data: " + ex.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error during loading initialization: {}", e.getMessage(), e);
            progressCallback.accept("Error loading data. Please try again.", 0.0);
            completionCallback.accept(false, "Error during initialization: " + e.getMessage());
        }
    }
    
    @Override
    public User getCurrentUser(BiConsumer<String, Double> progressCallback) {
        User currentUser = this.sessionService.getCurrentUser();
        
        if (currentUser == null) {
            logger.error("No user logged in, cannot preload chats. Trying to retrieve from session ID.");
            // Attempt to retrieve user by ID from session
            String userIdStr = sessionService.getElement("userId");
            
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    logger.debug("Found userId in session: {}", userId);
                    currentUser = userService.getUserById(userId);
                    
                    if (currentUser != null) {
                        logger.info("Successfully retrieved user from userId: {}", currentUser.getUsername());
                        // Store the user object in the session for future use
                        sessionService.setCurrentUser(currentUser);
                    } else {
                        logger.error("Failed to retrieve user with ID: {}", userId);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid userId format in session: {}", userIdStr);
                }
            } else {
                logger.error("No userId found in session");
            }
        } else {
            logger.info("Current user from session: {}", currentUser.getUsername());
        }
        
        return currentUser;
    }
    
    @Override
    public boolean connectWebSocket(User user, BiConsumer<String, Double> progressCallback) {
        progressCallback.accept("Connecting to chat server...", 0.05);
        try {
            boolean connectionInitiated = webSocketService.connect(user);
            
            if (connectionInitiated) {
                logger.info("WebSocket connection initiated for user: {}", user.getUsername());
            } else {
                logger.warn("Failed to initiate WebSocket connection for user: {}", user.getUsername());
                progressCallback.accept("Warning: Chat server connection failed, proceeding with offline mode", 0.05);
            }
            
            return connectionInitiated;
        } catch (Exception e) {
            logger.error("Failed to connect to WebSocket server: {}", e.getMessage(), e);
            progressCallback.accept("Warning: Chat server connection failed", 0.05);
            return false;
        }
    }
    
    @Override
    public CompletableFuture<Void> preloadUserChatsAsync(int userId, BiConsumer<String, Double> progressCallback) {
        // Update initial status
        progressCallback.accept("Loading user chats...", 0.1);
        
        // Create a CompletableFuture that will be completed when everything is loaded
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        
        // Load all user chats asynchronously
        chatService.getChatsByUserIdAsync(userId)
            .thenAccept(userChats -> {
                if (userChats.isEmpty()) {
                    // No chats to load, complete the future
                    progressCallback.accept("No conversations found", 1.0);
                    logger.info("No chats found for user ID: {}", userId);
                    resultFuture.complete(null); // Complete with null since it's a Void future
                    return;
                }
                
                progressCallback.accept("Found " + userChats.size() + " conversations", 0.2);
                logger.info("Found {} chats for user ID: {}", userChats.size(), userId);
                
                // Calculate progress increment per chat
                double progressPerChat = 0.8 / userChats.size();
                AtomicInteger completedChats = new AtomicInteger(0);
                
                // Create array to hold all futures for completion tracking
                CompletableFuture<List<Message>>[] messageFutures = new CompletableFuture[userChats.size()];
                
                // Store chat list for later joining after WebSocket connection is established
                List<Chat> chatsToJoin = new ArrayList<>(userChats);
                
                // Create a connection listener to handle chat joining after connection is established
                final ConnectionListener connectionListener = new ConnectionListener() {
                    @Override
                    public void onConnectionStatusChanged(boolean connected) {
                        if (connected) {
                            // Connection established, now safe to join chats
                            for (Chat chat : chatsToJoin) {
                                try {
                                    webSocketService.joinChat(chat);
                                    logger.debug("Joined WebSocket channel for chat: {}", chat.getName());
                                } catch (Exception e) {
                                    logger.error("Failed to join WebSocket channel for chat {}: {}", 
                                              chat.getName(), e.getMessage());
                                }
                            }
                            // Remove self after processing
                            webSocketService.removeConnectionListener(this);
                        }
                    }
                };
                
                // Add the connection listener
                webSocketService.addConnectionListener(connectionListener);
                
                // Try joining the chats if already connected
                if (webSocketService.isConnected()) {
                    for (Chat chat : chatsToJoin) {
                        try {
                            webSocketService.joinChat(chat);
                            logger.debug("Joined WebSocket channel for chat: {}", chat.getName());
                        } catch (Exception e) {
                            logger.error("Failed to join WebSocket channel for chat {}: {}", 
                                      chat.getName(), e.getMessage());
                            // Continue anyway - we can still load cached data
                        }
                    }
                }
                
                // Preload messages for each chat asynchronously
                for (int i = 0; i < userChats.size(); i++) {
                    final Chat chat = userChats.get(i);
                    
                    progressCallback.accept("Loading messages from " + chat.getName() + "...", 
                                       0.2 + (completedChats.get() * progressPerChat));
                    
                    // Store the future for later tracking
                    messageFutures[i] = messageService.getMessagesByChatIdAsync(chat.getId())
                        .thenApply(messages -> {
                            // Update progress on completion of each chat's message loading
                            int completed = completedChats.incrementAndGet();
                            progressCallback.accept("Loaded " + messages.size() + " messages from " + chat.getName(), 
                                           0.2 + (completed * progressPerChat));
                            logger.debug("Loaded {} messages from chat: {}", messages.size(), chat.getName());
                            return messages;
                        })
                        .exceptionally(ex -> {
                            logger.error("Error loading messages for chat {}: {}", chat.getName(), ex.getMessage());
                            completedChats.incrementAndGet();
                            return List.of(); // Return empty list on error
                        });
                }
                
                // When all message loading is complete, complete the result future
                CompletableFuture.allOf(messageFutures)
                    .thenRun(() -> {
                        // Final update
                        progressCallback.accept("All conversations loaded", 1.0);
                        logger.info("Successfully loaded all conversations for user ID: {}", userId);
                        
                        // Small delay for UX purposes
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // Complete the result future
                        resultFuture.complete(null);
                    })
                    .exceptionally(ex -> {
                        logger.error("Error during preloading: {}", ex.getMessage(), ex);
                        
                        // Complete the future exceptionally
                        resultFuture.completeExceptionally(ex);
                        return null;
                    });
            })
            .exceptionally(ex -> {
                logger.error("Error loading user chats: {}", ex.getMessage(), ex);
                
                // Complete the future exceptionally
                resultFuture.completeExceptionally(ex);
                return null;
            });
        
        return resultFuture;
    }
} 