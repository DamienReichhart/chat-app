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
 * Controller that handles message management operations like deletion
 */
@Controller
public class MessageManagementController extends BaseController {
    
    @Autowired
    public MessageManagementController(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
        logger.info("MessageManagementController initialized");
    }
    
    /**
     * Handle deleting a message in a chat room
     * @param chatId The chat room ID
     * @param message The message to delete
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/delete")
    @SendTo("/topic/chat/{chatId}/delete")
    public WebSocketMessageDTO deleteMessage(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, true);
        if (error != null) {
            return error;
        }
        
        logger.info("Message deleted in chat {} by user {}: {}", 
                chatId, 
                message.getUser().getUsername(),
                message.getMessage().getId());
        
        return message;
    }
} 