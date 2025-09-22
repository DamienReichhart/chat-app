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
 * Controller that handles sending and receiving messages
 */
@Controller
public class MessageController extends BaseController {
    
    @Autowired
    public MessageController(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
        logger.info("MessageController initialized");
    }
    
    /**
     * Handle sending a message to a chat room
     * @param chatId The chat room ID
     * @param message The message to send
     * @return The message to be broadcast to subscribers
     */
    @MessageMapping("/chat/{chatId}/message")
    @SendTo("/topic/chat/{chatId}/message")
    public WebSocketMessageDTO sendChatMessage(
            @DestinationVariable int chatId,
            @Payload WebSocketMessageDTO message) {
        
        WebSocketMessageDTO error = validateMessage(message, chatId, true);
        if (error != null) {
            return error;
        }
        
        logger.info("Message sent to chat {} by user {}: {}", 
                chatId, 
                message.getUser().getUsername(),
                message.getMessage().getContent());
        
        return message;
    }
} 