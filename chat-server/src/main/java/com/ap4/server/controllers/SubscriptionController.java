package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.server.interfaces.services.IChatSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller that handles WebSocket chat room subscriptions
 */
@Controller
public class SubscriptionController extends BaseController {
    private final IChatSessionService chatSessionService;
    
    @Autowired
    public SubscriptionController(SimpMessagingTemplate messagingTemplate, IChatSessionService chatSessionService) {
        super(messagingTemplate);
        this.chatSessionService = chatSessionService;
        logger.info("SubscriptionController initialized");
    }
    
    /**
     * Handle client joining a chat room
     * @param chatId The chat room ID
     * @param message The message containing user data
     * @param headerAccessor Headers to store session information
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/join")
    @SendTo("/topic/chat/{chatId}/join")
    public WebSocketMessageDTO joinChat(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, false);
        if (error != null) {
            return error;
        }
        
        // Store user info in WebSocket session
        headerAccessor.getSessionAttributes().put("username", message.getUser().getUsername());
        headerAccessor.getSessionAttributes().put("userId", message.getUser().getId());
        headerAccessor.getSessionAttributes().put("chatId", chatId);
        
        // Track session in service
        chatSessionService.addUserToChat(chatId, message.getUser().getId(), headerAccessor.getSessionId());
        
        logger.info("User {} joined chat {}", message.getUser().getUsername(), chatId);
        
        return message;
    }
    
    /**
     * Handle client leaving a chat room
     * @param chatId The chat room ID
     * @param message The message containing user data
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/leave")
    @SendTo("/topic/chat/{chatId}/leave")
    public WebSocketMessageDTO leaveChat(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, false);
        if (error != null) {
            return error;
        }
        
        // Remove user from chat tracking
        chatSessionService.removeUserFromChat(chatId, message.getUser().getId());
        
        logger.info("User {} left chat {}", message.getUser().getUsername(), chatId);
        
        return message;
    }
} 