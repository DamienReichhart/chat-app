package com.ap4.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ap4.client.interfaces.websocket.IWebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.config.WebSocketConfig;
import com.ap4.client.interfaces.websocket.ConnectionListener;
import com.ap4.client.interfaces.websocket.MessageListener;
import com.ap4.client.websocket.WebSocketHandler;
import com.ap4.client.interfaces.websocket.WebSocketListener;
import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import com.ap4.client.exceptions.websocket.WebSocketSubscriptionException;

/**
 * Service for managing WebSocket connection and messaging.
 * Provides methods for connecting to the server, joining chats, sending messages,
 * and handling WebSocket events.
 * 
 * Implements the Singleton pattern to ensure a single instance throughout the application.
 */
public class WebSocketService implements WebSocketListener {
    private static final Logger logger = LogManager.getLogger(WebSocketService.class);
    
    /**
     * Singleton instance of the WebSocketService.
     */
    private static WebSocketService instance;
    
    /**
     * The WebSocket handler that manages the actual connection.
     */
    private final IWebSocketHandler webSocketHandler;
    
    /**
     * List of listeners for message events.
     * Uses CopyOnWriteArrayList for thread-safety during iteration.
     */
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
    
    /**
     * List of listeners for connection events.
     * Uses CopyOnWriteArrayList for thread-safety during iteration.
     */
    private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    
    /**
     * The currently logged-in user.
     */
    private User currentUser;
    
    /**
     * List of chat IDs that the user has joined.
     */
    private List<Integer> joinedChats = new ArrayList<>();
    
    /**
     * Queue of chats waiting to be joined when connection is established.
     */
    private Queue<Chat> pendingJoins = new ConcurrentLinkedQueue<>();
    
    /**
     * WebSocket configuration.
     */
    private WebSocketConfig webSocketConfig;
    
    private final WebSocketExceptionHandler exceptionHandler;
    
    /**
     * Creates a new WebSocketService instance.
     * Private constructor to enforce the Singleton pattern.
     */
    private WebSocketService() {
        logger.info("Initializing WebSocketService");
        webSocketHandler = WebSocketHandler.getInstance();
        webSocketHandler.addListener(this);
        this.exceptionHandler = new WebSocketExceptionHandler();
    }
    
    /**
     * Gets the singleton instance of the WebSocketService.
     * 
     * @return The singleton instance
     */
    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }
    
    /**
     * Initializes the WebSocket service with configuration and session.
     * 
     * @param webSocketConfig The WebSocket configuration
     * @param sessionService The session service for authentication information
     */
    public void initialize(WebSocketConfig webSocketConfig, SessionService sessionService) {
        logger.info("Initializing WebSocketService with configuration");
        this.webSocketConfig = webSocketConfig;
        this.webSocketHandler.initialize(webSocketConfig);
        
        // If user is already logged in, connect automatically
        if (sessionService != null && sessionService.isLoggedIn()) {
            try {
                User currentUser = sessionService.getCurrentUser();
                connect(currentUser);
            } catch (Exception e) {
                logger.warn("Could not auto-connect with current user: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Connects to the WebSocket server with the specified user.
     * 
     * @param user The user to connect with
     * @return true if connection attempt was initiated successfully
     */
    public boolean connect(User user) {
        if (user == null) {
            logger.warn("Cannot connect with null user");
            return false;
        }
        
        try {
            logger.info("Connecting to WebSocket server with user: {} (ID: {})", user.getUsername(), user.getId());
            this.currentUser = user;
            
            // Check if WebSocketHandler is properly initialized
            if (webSocketHandler == null) {
                logger.error("WebSocketHandler is null, cannot connect");
                return false;
            }
            
            // Check if configuration is available
            if (webSocketConfig == null) {
                logger.warn("WebSocketConfig is null, using default configuration");
                // You may want to set a default configuration here if needed
            }
            
            // Attempt the connection - this is asynchronous
            webSocketHandler.connect(user);
            
            logger.debug("WebSocket connection initiated for user: {}", user.getUsername());
            return true;  // Return true if we at least started the connection process
        } catch (Exception e) {
            logger.error("Exception during WebSocket connection: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Disconnects from the WebSocket server.
     */
    public void disconnect() {
        logger.info("Disconnecting from WebSocket server");
        reset();
        webSocketHandler.disconnect();
    }
    
    /**
     * Completely resets the service state.
     * Clears all tracked chats, user data, and listener notifications.
     */
    public void reset() {
        logger.info("Resetting WebSocketService state");
        joinedChats.clear();
        this.currentUser = null;
        
        // Notify listeners about disconnection if they haven't been notified
        if (webSocketHandler.isConnected()) {
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnectionStatusChanged(false);
                } catch (Exception e) {
                    logger.error("Error notifying connection listener during reset: {}", e.getMessage(), e);
                }
            }
        }
        
        // Reset the underlying WebSocket handler
        webSocketHandler.reset();
    }
    
    public void joinChat(Chat chat) {
        if (chat == null) {
            logger.warn("Cannot join null chat");
            return;
        }
        
        logger.info("Joining chat: {} ({})", chat.getName(), chat.getId());
        
        if (!webSocketHandler.isConnected()) {
            logger.info("Not connected, queuing chat join request for: {} ({})", chat.getName(), chat.getId());
            pendingJoins.offer(chat);
            return;
        }
        
        try {
            webSocketHandler.joinChat(chat);
            
            // Track joined chats
            if (!joinedChats.contains(chat.getId())) {
                joinedChats.add(chat.getId());
            }
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            // Queue for retry if it's a connection issue
            if (e instanceof WebSocketSubscriptionException) {
                pendingJoins.offer(chat);
            }
        }
    }
    
    public void joinChat(int chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);
        joinChat(chat);
    }
    
    public void leaveChat(Chat chat) {
        if (chat == null) {
            logger.warn("Cannot leave null chat");
            return;
        }
        
        try {
            logger.info("Leaving chat: {} ({})", chat.getName(), chat.getId());
            webSocketHandler.leaveChat(chat);
            
            // Remove from tracked chats
            joinedChats.remove(Integer.valueOf(chat.getId()));
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
        }
    }
    
    public void sendMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot send null message");
            return;
        }

        // Create a new Message without the file data
        Message sendedMessage = new Message();
        sendedMessage.setChat(message.getChat());
        sendedMessage.setContent(message.getContent());
        sendedMessage.setContentType(message.getContentType());
        sendedMessage.setId(message.getId());
        sendedMessage.setPinned(message.isPinned());
        sendedMessage.setSender(message.getSender());
        sendedMessage.setDeleted(message.isDeleted());
        sendedMessage.setFileName(message.getFileName());
        sendedMessage.setFileType(message.getFileType());
        sendedMessage.setTimestamp(message.getTimestamp());
        sendedMessage.setAnonymous(message.isAnonymous());

        try {
            logger.info("Sending message to chat {}: {}", sendedMessage.getChat().getId(), sendedMessage.getContent());
            webSocketHandler.sendMessage(sendedMessage);
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            // Handle potential reconnection needs
            if (!webSocketHandler.isConnected() && currentUser != null) {
                connect(currentUser);
            }
        }
    }
    
    public void sendMessageDTO(WebSocketMessageDTO dto, String destination) {
        if (dto == null || destination == null) {
            logger.warn("Cannot send null DTO or destination");
            return;
        }
        
        try {
            logger.info("Sending WebSocket message to {}: type={}", destination);
            webSocketHandler.sendMessageDTO(dto, destination);
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            // Handle potential reconnection needs
            if (!webSocketHandler.isConnected() && currentUser != null) {
                connect(currentUser);
            }
        }
    }
    
    public void pinMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot pin null message");
            return;
        }
        
        try {
            logger.info("Pinning message {}", message.getId());
            webSocketHandler.pinMessage(message);
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            // Handle potential reconnection needs
            if (!webSocketHandler.isConnected() && currentUser != null) {
                connect(currentUser);
            }
        }
    }
    
    public void unpinMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot unpin null message");
            return;
        }
        
        try {
            logger.info("Unpinning message {}", message.getId());
            webSocketHandler.unpinMessage(message);
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            // Handle potential reconnection needs
            if (!webSocketHandler.isConnected() && currentUser != null) {
                connect(currentUser);
            }
        }
    }
    
    public void deleteMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot delete null message");
            return;
        }
        
        try {
            logger.info("Deleting message {}", message.getId());
            webSocketHandler.deleteMessage(message);
        } catch (Exception e) {
            logger.error("WebSocket error: {}", e.getMessage(), e);
            
            // Handle potential reconnection needs
            if (!webSocketHandler.isConnected() && currentUser != null) {
                connect(currentUser);
            }
        }
    }
    
    public boolean isConnected() {
        return webSocketHandler.isConnected();
    }
    
    public boolean isInChat(int chatId) {
        return joinedChats.contains(chatId);
    }
    
    @Override
    public void onConnectionStatusChanged(boolean connected) {
        logger.info("Connection status changed: connected={}", connected);
        
        // Notify all connection listeners
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onConnectionStatusChanged(connected);
            } catch (Exception e) {
                logger.error("Error notifying connection listener: {}", e.getMessage(), e);
            }
        }
        
        // Process pending chat joins if connection is established
        if (connected && !pendingJoins.isEmpty()) {
            logger.info("Processing {} pending chat joins", pendingJoins.size());
            processPendingJoins();
        }
    }
    
    @Override
    public void onMessageReceived(Message message) {
        logger.info("Received message: {}", message.getId());
        
        // Forward to all message listeners
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                logger.error("Error notifying message listener: {}", e.getMessage(), e);
            }
        }
        
        // Track the chat if this is a message from a newly joined chat
        if (message.getChat() != null && !joinedChats.contains(message.getChat().getId())) {
            logger.info("Auto-tracking chat from received message: {}", message.getChat().getId());
            joinedChats.add(message.getChat().getId());
        }
    }
    
    @Override
    public void onError(String errorMessage) {
        logger.error("WebSocket error: {}", errorMessage);
        
        // Try to handle the error based on type
        try {
            String userFriendlyError = exceptionHandler.handleMessage(errorMessage);
            logger.warn("Converted error message: {}", userFriendlyError);
            
            // Add code here to show the error to the user if needed
        } catch (Exception e) {
            logger.error("Error handling WebSocket error: {}", e.getMessage(), e);
        }
    }
    
    public void addMessageListener(MessageListener listener) {
        if (listener != null && !messageListeners.contains(listener)) {
            messageListeners.add(listener);
            logger.debug("Added message listener, total: {}", messageListeners.size());
        }
    }
    
    public void removeMessageListener(MessageListener listener) {
        if (listener != null) {
            messageListeners.remove(listener);
            logger.debug("Removed message listener, remaining: {}", messageListeners.size());
        }
    }
    
    public void addConnectionListener(ConnectionListener listener) {
        if (listener != null && !connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
            logger.debug("Added connection listener, total: {}", connectionListeners.size());
            
            // Immediately notify of current connection status
            if (webSocketHandler != null) {
                listener.onConnectionStatusChanged(webSocketHandler.isConnected());
            }
        }
    }
    
    public void removeConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.remove(listener);
            logger.debug("Removed connection listener, remaining: {}", connectionListeners.size());
        }
    }
    
    private void processPendingJoins() {
        logger.info("Processing pending chat joins");
        
        while (!pendingJoins.isEmpty()) {
            Chat chat = pendingJoins.poll();
            if (chat != null) {
                try {
                    logger.info("Joining previously queued chat: {} ({})", chat.getName(), chat.getId());
                    webSocketHandler.joinChat(chat);
                    
                    // Track joined chat
                    if (!joinedChats.contains(chat.getId())) {
                        joinedChats.add(chat.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error joining queued chat {}: {}", chat.getId(), e.getMessage(), e);
                    
                    // Re-queue for later attempt if still a connection issue
                    if (!webSocketHandler.isConnected()) {
                        pendingJoins.offer(chat);
                        break;  // Stop processing and wait for next connection event
                    }
                }
            }
        }
    }
} 