package com.ap4.client.websocket.processor;

import com.ap4.client.exceptions.websocket.WebSocketMessageException;
import com.ap4.client.exceptions.websocket.WebSocketSubscriptionException;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.websocket.IWebSocketConnectionManager;
import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import com.ap4.client.interfaces.websocket.MessageListener;
import com.ap4.client.services.MessageService;
import com.ap4.client.websocket.frame.ChatFrameHandler;
import com.ap4.client.websocket.frame.DeleteMessageFrameHandler;
import com.ap4.client.websocket.frame.JoinChatFrameHandler;
import com.ap4.client.websocket.frame.LeaveChatFrameHandler;
import com.ap4.client.websocket.frame.MessageFrameHandler;
import com.ap4.client.websocket.frame.PinMessageFrameHandler;
import com.ap4.client.websocket.frame.UnpinMessageFrameHandler;
import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.enums.ContentType;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Processes WebSocket messages for sending and receiving
 */
public class WebSocketMessageProcessor implements IWebSocketMessageProcessor {
    private static final Logger logger = LogManager.getLogger(WebSocketMessageProcessor.class);
    private static IWebSocketMessageProcessor instance;
    
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final IWebSocketConnectionManager connectionManager;
    private final Map<Integer, Map<String, StompSession.Subscription>> chatSubscriptions = new HashMap<>();
    
    /**
     * Private constructor for singleton
     */
    private WebSocketMessageProcessor(IWebSocketConnectionManager connectionManager) {
        logger.info("Initializing WebSocketMessageProcessor");
        this.connectionManager = connectionManager;
    }
    
    /**
     * Get singleton instance
     * @param connectionManager The connection manager to use
     * @return WebSocketMessageProcessor instance
     */
    public static synchronized IWebSocketMessageProcessor getInstance(IWebSocketConnectionManager connectionManager) {
        if (instance == null) {
            instance = new WebSocketMessageProcessor(connectionManager);
        }
        return instance;
    }
    
    /**
     * Helper method to load file data for a message if needed
     * @param message The message that might need file data loaded
     * @return The message with file data loaded if needed
     */
    private Message loadFileDataIfNeeded(Message message) {
        if (message != null && message.getFileData() == null && 
            (message.getContentType() == ContentType.FILE || message.getContentType() == ContentType.IMAGE)) {
            logger.info("Message is a file/image without data, loading from database: messageId={}", message.getId());
            
            try {
                // Get message service to load file data
                IMessageService messageService = new MessageService();
                message = messageService.getMessageById(message.getId());
                logger.info("Loaded file data for message: {}", message.getId());
            } catch (Exception e) {
                logger.error("Error loading file data for message {}: {}", message.getId(), e.getMessage(), e);
            }
        }
        return message;
    }
    
    @Override
    public void processChatMessage(WebSocketMessageDTO dto) {
        if (dto == null || dto.getMessage() == null) {
            logger.warn("Received null WebSocket message or message content");
            return;
        }
        
        try {
            Message message = dto.getMessage();
            logger.info("Processing chat message: chatId={}, content={}, messageId={}",
                message.getChat() != null ? message.getChat().getId() : "null",
                message.getContent(),
                message.getId());
                
            // Load file data if needed
            message = loadFileDataIfNeeded(message);
            
            // Notify listeners about the received message
            notifyMessageReceived(message);
        } catch (Exception e) {
            logger.error("Error processing chat message: {}", e.getMessage(), e);
            notifyError("Error processing chat message: " + e.getMessage());
        }
    }
    
    @Override
    public void processJoinChatEvent(WebSocketMessageDTO dto) {
        if (dto == null) {
            logger.warn("Received null WebSocket message DTO");
            return;
        }
        
        try {
            User user = dto.getUser();
            int chatId = dto.getChatId();
            
            logger.info("Processing join chat event: chatId={}, user={}",
                chatId,
                user != null ? user.getUsername() : "unknown");
            
            // Here you can add specific handling for join events
            // For example, update UI, notify user, update participant list, etc.
            
            // If the join event contains a system message, process it
            if (dto.getMessage() != null) {
                Message message = dto.getMessage();
                notifyMessageReceived(message);
            }
        } catch (Exception e) {
            logger.error("Error processing join chat event: {}", e.getMessage(), e);
            notifyError("Error processing join event: " + e.getMessage());
        }
    }
    
    @Override
    public void processLeaveChatEvent(WebSocketMessageDTO dto) {
        if (dto == null) {
            logger.warn("Received null WebSocket message DTO");
            return;
        }
        
        try {
            User user = dto.getUser();
            int chatId = dto.getChatId();
            
            logger.info("Processing leave chat event: chatId={}, user={}",
                chatId,
                user != null ? user.getUsername() : "unknown");
            
            // Here you can add specific handling for leave events
            // For example, update UI, notify user, update participant list, etc.
            
            // If the leave event contains a system message, process it
            if (dto.getMessage() != null) {
                Message message = dto.getMessage();
                notifyMessageReceived(message);
            }
        } catch (Exception e) {
            logger.error("Error processing leave chat event: {}", e.getMessage(), e);
            notifyError("Error processing leave event: " + e.getMessage());
        }
    }
    
    @Override
    public void processPinMessageEvent(WebSocketMessageDTO dto) {
        if (dto == null || dto.getMessage() == null) {
            logger.warn("Received null WebSocket message or message content for pin event");
            return;
        }
        
        try {
            Message message = dto.getMessage();
            User user = dto.getUser();
            
            logger.info("Processing pin message event: chatId={}, messageId={}, pinnedBy={}",
                dto.getChatId(),
                message.getId(),
                user != null ? user.getUsername() : "unknown");
                
            // Update the pin status on the message
            message.setPinned(true);
            
            // Always notify listeners to update the UI
            notifyMessageReceived(message);
        } catch (Exception e) {
            logger.error("Error processing pin message event: {}", e.getMessage(), e);
            notifyError("Error processing pin event: " + e.getMessage());
        }
    }
    
    @Override
    public void processUnpinMessageEvent(WebSocketMessageDTO dto) {
        if (dto == null || dto.getMessage() == null) {
            logger.warn("Received null WebSocket message or message content for unpin event");
            return;
        }
        
        try {
            Message message = dto.getMessage();
            User user = dto.getUser();
            
            logger.info("Processing unpin message event: chatId={}, messageId={}, unpinnedBy={}",
                dto.getChatId(),
                message.getId(),
                user != null ? user.getUsername() : "unknown");
                
            // Update the pin status on the message
            message.setPinned(false);
            
            // Always notify listeners to update the UI
            notifyMessageReceived(message);
        } catch (Exception e) {
            logger.error("Error processing unpin message event: {}", e.getMessage(), e);
            notifyError("Error processing unpin event: " + e.getMessage());
        }
    }
    
    @Override
    public void processDeleteMessageEvent(WebSocketMessageDTO dto) {
        if (dto == null || dto.getMessage() == null) {
            logger.warn("Received null WebSocket message or message content for delete event");
            return;
        }
        
        try {
            Message message = dto.getMessage();
            User user = dto.getUser();
            
            logger.info("Processing delete message event: chatId={}, messageId={}, deletedBy={}",
                dto.getChatId(),
                message.getId(),
                user != null ? user.getUsername() : "unknown");
                
            // Mark the message as deleted
            message.setDeleted(true);
            
            // Always notify listeners to update the UI by removing the deleted message
            notifyMessageReceived(message);
        } catch (Exception e) {
            logger.error("Error processing delete message event: {}", e.getMessage(), e);
            notifyError("Error processing delete event: " + e.getMessage());
        }
    }
    
    @Override
    public void joinChat(Chat chat) {
        if (chat == null) {
            logger.warn("Cannot join null chat");
            return;
        }
        
        StompSession session = connectionManager.getStompSession();
        if (session == null || !connectionManager.isConnected()) {
            logger.warn("Cannot join chat: not connected");
            throw new WebSocketSubscriptionException(
                "Not connected to server"
            );
        }
        
        try {
            logger.info("Joining chat room: {} ({})", chat.getName(), chat.getId());
            
            // Initialize subscriptions map for this chat if needed
            if (!chatSubscriptions.containsKey(chat.getId())) {
                chatSubscriptions.put(chat.getId(), new HashMap<>());
            }
            
            Map<String, StompSession.Subscription> subscriptions = chatSubscriptions.get(chat.getId());
            
            // Subscribe to all the required endpoints for this chat
            subscribeToEndpoint(session, chat, "join", new JoinChatFrameHandler(this), subscriptions);
            subscribeToEndpoint(session, chat, "leave", new LeaveChatFrameHandler(this), subscriptions);
            subscribeToEndpoint(session, chat, "message", new MessageFrameHandler(this), subscriptions);
            subscribeToEndpoint(session, chat, "pin", new PinMessageFrameHandler(this), subscriptions);
            subscribeToEndpoint(session, chat, "unpin", new UnpinMessageFrameHandler(this), subscriptions);
            subscribeToEndpoint(session, chat, "delete", new DeleteMessageFrameHandler(this), subscriptions);
            
            // Send a join message to the server
            WebSocketMessageDTO joinDto = new WebSocketMessageDTO();
            joinDto.setChatId(chat.getId());
            joinDto.setUser(connectionManager.getCurrentUser());
            
            sendMessageDTO(joinDto, "/app/chat/" + chat.getId() + "/join");
            
            logger.info("Successfully joined chat: {} ({})", chat.getName(), chat.getId());
        } catch (Exception e) {
            logger.error("Error joining chat {}: {}", chat.getId(), e.getMessage(), e);
            
            // Clean up any partial subscriptions
            try {
                unsubscribeFromChat(chat.getId());
            } catch (Exception ex) {
                logger.warn("Error cleaning up chat subscription: {}", ex.getMessage());
            }
            
            throw new WebSocketSubscriptionException(
                "Failed to join chat: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Subscribe to a specific endpoint for a chat
     * 
     * @param session The STOMP session
     * @param chat The chat to subscribe to
     * @param endpoint The endpoint name (join, leave, message, etc.)
     * @param handler The frame handler to use
     * @param subscriptions Map to store the subscription
     */
    private void subscribeToEndpoint(
            StompSession session, 
            Chat chat, 
            String endpoint, 
            ChatFrameHandler handler,
            Map<String, StompSession.Subscription> subscriptions) {
        
        String destination = "/topic/chat/" + chat.getId() + "/" + endpoint;
        logger.info("Subscribing to {}", destination);
        
        StompSession.Subscription subscription = session.subscribe(destination, handler);
        subscriptions.put(endpoint, subscription);
    }
    
    /**
     * Unsubscribe from all endpoints for a chat
     * 
     * @param chatId The chat ID to unsubscribe from
     */
    private void unsubscribeFromChat(int chatId) {
        if (chatSubscriptions.containsKey(chatId)) {
            Map<String, StompSession.Subscription> subscriptions = chatSubscriptions.get(chatId);
            
            for (Map.Entry<String, StompSession.Subscription> entry : subscriptions.entrySet()) {
                try {
                    logger.info("Unsubscribing from endpoint: {}", entry.getKey());
                    entry.getValue().unsubscribe();
                } catch (Exception e) {
                    logger.warn("Error unsubscribing from {}: {}", entry.getKey(), e.getMessage());
                }
            }
            
            subscriptions.clear();
            chatSubscriptions.remove(chatId);
        }
    }
    
    @Override
    public void leaveChat(Chat chat) {
        if (chat == null) {
            logger.warn("Cannot leave null chat");
            return;
        }
        
        try {
            logger.info("Leaving chat: {} ({})", chat.getName(), chat.getId());
            
            // Send a leave message to the server
            if (connectionManager.isConnected()) {
                WebSocketMessageDTO leaveDto = new WebSocketMessageDTO();
                leaveDto.setChatId(chat.getId());
                leaveDto.setUser(connectionManager.getCurrentUser());
                
                try {
                    sendMessageDTO(leaveDto, "/app/chat/" + chat.getId() + "/leave");
                } catch (Exception e) {
                    logger.warn("Error sending leave notification: {}", e.getMessage());
                }
            }
            
            // Unsubscribe from all endpoints for this chat
            unsubscribeFromChat(chat.getId());
            logger.info("Unsubscribed from chat: {}", chat.getId());
        } catch (Exception e) {
            logger.error("Error leaving chat {}: {}", chat.getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public void sendMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot send null message");
            return;
        }
        
        if (!connectionManager.isConnected()) {
            logger.warn("Cannot send message: not connected");
            throw new WebSocketMessageException("Not connected to server");
        }
        
        try {
            // Create the WebSocket message DTO
            WebSocketMessageDTO dto = new WebSocketMessageDTO();
            dto.setChatId(message.getChat().getId());
            dto.setMessage(message);
            dto.setUser(connectionManager.getCurrentUser());
            
            // Send the message
            sendMessageDTO(dto, "/app/chat/" + message.getChat().getId() + "/message");
            
            logger.info("Message sent to chat {}: {}", message.getChat().getId(), message.getContent());
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            throw new WebSocketMessageException("Failed to send message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void sendMessageDTO(WebSocketMessageDTO dto, String destination) {
        if (dto == null || destination == null) {
            logger.warn("Cannot send null DTO or destination");
            return;
        }

        StompSession session = connectionManager.getStompSession();
        if (session == null || !connectionManager.isConnected()) {
            logger.warn("Cannot send message: not connected");
            throw new WebSocketMessageException("Not connected to server");
        }
        
        try {
            logger.info("Sending WebSocket message to {}", destination);
            session.send(destination, dto);
        } catch (Exception e) {
            logger.error("Error sending WebSocket message to {}: {}", destination, e.getMessage(), e);
            throw new WebSocketMessageException("Failed to send message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void pinMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot pin null message");
            return;
        }
        
        if (!connectionManager.isConnected()) {
            logger.warn("Cannot pin message: not connected");
            throw new WebSocketMessageException("Not connected to server");
        }
        
        try {
            // Create the WebSocket message DTO
            WebSocketMessageDTO dto = new WebSocketMessageDTO();
            dto.setChatId(message.getChat().getId());
            dto.setMessage(message);
            dto.setUser(connectionManager.getCurrentUser());
            
            // Send the pin message
            sendMessageDTO(dto, "/app/chat/" + message.getChat().getId() + "/pin");
            
            logger.info("Pin message sent for message {}", message.getId());
        } catch (Exception e) {
            logger.error("Error pinning message: {}", e.getMessage(), e);
            throw new WebSocketMessageException("Failed to pin message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void unpinMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot unpin null message");
            return;
        }
        
        if (!connectionManager.isConnected()) {
            logger.warn("Cannot unpin message: not connected");
            throw new WebSocketMessageException("Not connected to server");
        }
        
        try {
            // Create the WebSocket message DTO
            WebSocketMessageDTO dto = new WebSocketMessageDTO();
            dto.setChatId(message.getChat().getId());
            dto.setMessage(message);
            dto.setUser(connectionManager.getCurrentUser());
            
            // Send the unpin message
            sendMessageDTO(dto, "/app/chat/" + message.getChat().getId() + "/unpin");
            
            logger.info("Unpin message sent for message {}", message.getId());
        } catch (Exception e) {
            logger.error("Error unpinning message: {}", e.getMessage(), e);
            throw new WebSocketMessageException("Failed to unpin message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteMessage(Message message) {
        if (message == null) {
            logger.warn("Cannot delete null message");
            return;
        }
        
        if (!connectionManager.isConnected()) {
            logger.warn("Cannot delete message: not connected");
            throw new WebSocketMessageException("Not connected to server");
        }
        
        try {
            // Create the WebSocket message DTO
            WebSocketMessageDTO dto = new WebSocketMessageDTO();
            dto.setChatId(message.getChat().getId());
            dto.setMessage(message);
            dto.setUser(connectionManager.getCurrentUser());
            
            // Send the delete message
            sendMessageDTO(dto, "/app/chat/" + message.getChat().getId() + "/delete");
            
            logger.info("Delete message sent for message {}", message.getId());
        } catch (Exception e) {
            logger.error("Error deleting message: {}", e.getMessage(), e);
            throw new WebSocketMessageException("Failed to delete message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        if (listener != null && !messageListeners.contains(listener)) {
            messageListeners.add(listener);
        }
    }
    
    @Override
    public void removeMessageListener(MessageListener listener) {
        if (listener != null) {
            messageListeners.remove(listener);
        }
    }
    
    @Override
    public void notifyMessageReceived(Message message) {
        if (message == null) {
            return;
        }
        
        logger.debug("Notifying {} listeners about received message", messageListeners.size());
        
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                logger.error("Error notifying message listener: {}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void notifyError(String errorMessage) {
        logger.error("WebSocket error: {}", errorMessage);
    }
} 