package com.ap4.server.controllers;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Base controller class providing common functionality for WebSocket controllers
 */
public abstract class BaseController {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final SimpMessagingTemplate messagingTemplate;
    
    protected BaseController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Create an error message DTO
     * @param errorMessage The error message
     * @return WebSocketMessageDTO with error type and message
     */
    protected WebSocketMessageDTO createErrorMessage(String errorMessage) {
        logger.error("WebSocket error: {}", errorMessage);
        
        Message message = new Message();
        message.setContent(errorMessage);
        
        WebSocketMessageDTO dto = new WebSocketMessageDTO();
        dto.setMessage(message);
        dto.setError(errorMessage);
        
        return dto;
    }
    
    /**
     * Validate basic message requirements
     * @param message The message to validate
     * @param chatId The chat ID
     * @param requireMessage Whether a message object is required
     * @return Error message DTO or null if valid
     */
    protected WebSocketMessageDTO validateMessage(WebSocketMessageDTO message, int chatId, boolean requireMessage) {
        if (message.getUser() == null) {
            logger.warn("WebSocket request to chat {} without user information", chatId);
            return createErrorMessage("User information missing");
        }
        
        if (requireMessage && message.getMessage() == null) {
            logger.warn("WebSocket request to chat {} with missing message content", chatId);
            return createErrorMessage("Message content missing");
        }
        
        return null;
    }
} 