package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller that handles pinning and unpinning messages
 */
@Controller
public class PinController extends BaseController {
    
    @Autowired
    public PinController(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
        logger.info("PinController initialized");
    }
    
    /**
     * Handle pinning a message in a chat room
     * @param chatId The chat room ID
     * @param message The message to pin
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/pin")
    @SendTo("/topic/chat/{chatId}/pin")
    public WebSocketMessageDTO pinMessage(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, true);
        if (error != null) {
            return error;
        }
        
        // Set pinned status
        message.getMessage().setPinned(true);
        
        logger.info("Message pinned in chat {} by user {}: {}", 
                chatId, 
                message.getUser().getUsername(),
                message.getMessage().getId());
        
        return message;
    }
    
    /**
     * Handle unpinning a message in a chat room
     * @param chatId The chat room ID
     * @param message The message to unpin
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/unpin")
    @SendTo("/topic/chat/{chatId}/unpin")
    public WebSocketMessageDTO unpinMessage(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, true);
        if (error != null) {
            return error;
        }
        
        // Set pinned status
        message.getMessage().setPinned(false);
        
        logger.info("Message unpinned in chat {} by user {}: {}", 
                chatId, 
                message.getUser().getUsername(),
                message.getMessage().getId());
        
        return message;
    }
} 