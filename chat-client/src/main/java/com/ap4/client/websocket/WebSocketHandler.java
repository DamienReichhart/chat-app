package com.ap4.client.websocket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ap4.client.interfaces.websocket.IWebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompSession;

import com.ap4.client.config.WebSocketConfig;
import com.ap4.client.interfaces.websocket.IWebSocketConnectionManager;
import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import com.ap4.client.interfaces.websocket.WebSocketListener;
import com.ap4.client.websocket.connection.WebSocketConnectionManager;
import com.ap4.client.websocket.processor.WebSocketMessageProcessor;
import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Main WebSocket handler that delegates responsibilities to specialized components.
 * Acts as a facade for the WebSocket subsystem.
 */
public class WebSocketHandler implements IWebSocketHandler {
    private static final Logger logger = LogManager.getLogger(WebSocketHandler.class);
    private static IWebSocketHandler instance;
    
    private final List<WebSocketListener> listeners = new CopyOnWriteArrayList<>();
    private final IWebSocketConnectionManager connectionManager;
    private final IWebSocketMessageProcessor messageProcessor;

    /**
     * Private constructor for singleton pattern
     */
    private WebSocketHandler() {
        logger.info("Initializing WebSocketHandler");
        
        // Initialize connection manager
        this.connectionManager = WebSocketConnectionManager.getInstance();
        
        // Initialize message processor with the connection manager
        this.messageProcessor = WebSocketMessageProcessor.getInstance(connectionManager);
    }
    
    /**
     * Get singleton instance
     * @return WebSocketHandler instance
     */
    public static synchronized IWebSocketHandler getInstance() {
        if (instance == null) {
            instance = new WebSocketHandler();
        }
        return instance;
    }
    
    @Override
    public void initialize(WebSocketConfig config) {
        logger.info("Initializing WebSocketHandler with configuration");
        connectionManager.initialize(config);
    }
    
    @Override
    public void connect(User user) {
        logger.info("Connecting to WebSocket server with user: {}", user.getUsername());
        connectionManager.connect(user);
    }
    
    @Override
    public void disconnect() {
        logger.info("Disconnecting from WebSocket server");
        connectionManager.disconnect();
    }
    
    @Override
    public void reset() {
        logger.info("Resetting WebSocketHandler");
        connectionManager.reset();
    }
    
    @Override
    public void joinChat(Chat chat) {
        logger.info("Joining chat: {} ({})", chat.getName(), chat.getId());
        messageProcessor.joinChat(chat);
    }
    
    @Override
    public void leaveChat(Chat chat) {
        logger.info("Leaving chat: {} ({})", chat.getName(), chat.getId());
        messageProcessor.leaveChat(chat);
    }
    
    @Override
    public void sendMessage(Message message) {
        logger.info("Sending message to chat: {}", message.getChat().getId());
        messageProcessor.sendMessage(message);
    }
    
    @Override
    public void sendMessageDTO(WebSocketMessageDTO dto, String destination) {
        logger.info("Sending WebSocket message to {}: type={}", destination);
        messageProcessor.sendMessageDTO(dto, destination);
    }
    
    @Override
    public void pinMessage(Message message) {
        logger.info("Pinning message: {}", message.getId());
        messageProcessor.pinMessage(message);
    }
    
    @Override
    public void unpinMessage(Message message) {
        logger.info("Unpinning message: {}", message.getId());
        messageProcessor.unpinMessage(message);
    }
    
    @Override
    public void deleteMessage(Message message) {
        logger.info("Deleting message: {}", message.getId());
        messageProcessor.deleteMessage(message);
    }
    
    @Override
    public void addListener(WebSocketListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            
            // Add as message and connection listener to the appropriate components
            messageProcessor.addMessageListener(listener::onMessageReceived);
            connectionManager.addConnectionListener(listener::onConnectionStatusChanged);
        }
    }
    
    @Override
    public void removeListener(WebSocketListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    @Override
    public boolean isConnected() {
        return connectionManager.isConnected();
    }
    
    @Override
    public User getCurrentUser() {
        return connectionManager.getCurrentUser();
    }
    
    @Override
    public void handleSuccessfulConnection(StompSession session) {
        connectionManager.handleSuccessfulConnection(session);
    }
    
    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        connectionManager.handleTransportError(session, exception);
    }
    
    @Override
    public void processReceivedMessage(WebSocketMessageDTO dto) {
        if (dto == null) {
            logger.warn("Received null WebSocket message DTO");
            return;
        }
        
        try {
            logger.info("Processing received WebSocket message: chatId={}", dto.getChatId());
            
            // Try to determine the message type from the message content
            if (dto.getMessage() == null) {
                // If there's no message, it might be a join/leave event
                // Default to join event as it's more common
                messageProcessor.processJoinChatEvent(dto);
            } else {
                Message message = dto.getMessage();
                
                // Try to determine message type based on message properties
                if (message.isDeleted()) {
                    messageProcessor.processDeleteMessageEvent(dto);
                } else if (message.isPinned()) {
                    // This could be a pin or unpin event, but we'll assume pin
                    // The actual pin/unpin status should be handled in the processor method
                    messageProcessor.processPinMessageEvent(dto);
                } else {
                    // Default to regular chat message
                    messageProcessor.processChatMessage(dto);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing received message: {}", e.getMessage(), e);
            notifyError("Error processing message: " + e.getMessage());
        }
    }
    
    @Override
    public void notifyConnectionStatus(boolean connected) {
        // This is now handled by the connection manager
    }
    
    @Override
    public void notifyMessageReceived(Message message) {
        // This is now handled by the message processor
    }
    
    @Override
    public void notifyError(String errorMessage) {
        logger.error("WebSocket error: {}", errorMessage);
        
        // Notify all listeners
        for (WebSocketListener listener : listeners) {
            try {
                listener.onError(errorMessage);
            } catch (Exception e) {
                logger.error("Error notifying error listener: {}", e.getMessage(), e);
            }
        }
    }
}
