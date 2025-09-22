package com.ap4.server.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.User;
import com.ap4.server.interfaces.services.IChatSessionService;

/**
 * Listener for WebSocket events like user disconnections
 */
@Component
public class WebSocketEventListener {
    private static final Logger logger = LogManager.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final IChatSessionService chatSessionService;

    @Autowired
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, IChatSessionService chatSessionService) {
        this.messagingTemplate = messagingTemplate;
        this.chatSessionService = chatSessionService;
        logger.info("WebSocketEventListener initialized");
    }

    /**
     * Handle WebSocket disconnect events to notify other users when someone disconnects
     * 
     * @param event The disconnect event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Log the disconnection
        logger.info("Client disconnected: {}", sessionId);
        
        // Check session attributes
        if (headerAccessor.getSessionAttributes() != null) {
            // Get user information from session
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
            Integer chatId = (Integer) headerAccessor.getSessionAttributes().get("chatId");
            
            // If we have user and chat info, notify others
            if (username != null && userId != null && chatId != null) {
                logger.info("User disconnected: {} ({}), chat: {}", username, userId, chatId);
                
                // Create user and message objects for the notification
                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                
                // Create a leave message
                WebSocketMessageDTO leaveMessage = new WebSocketMessageDTO();
                leaveMessage.setUser(user);
                leaveMessage.setChatId(chatId);
                
                // Send notification to the appropriate chat channel
                messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/leave", leaveMessage);
                
                // Update session tracking
                chatSessionService.removeUserFromChat(chatId, userId);
                
                logger.info("Leave notification sent for user: {} ({})", username, userId);
            } else {
                logger.info("Client disconnected without complete user session info");
            }
        }
    }
} 