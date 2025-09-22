package com.ap4.client.websocket.frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import com.ap4.common.dto.WebSocketMessageDTO;

/**
 * Specialized frame handler for chat messages
 */
public class MessageFrameHandler extends ChatFrameHandler {
    private static final Logger logger = LogManager.getLogger(MessageFrameHandler.class);
    
    /**
     * Constructor
     * 
     * @param messageProcessor The WebSocketMessageProcessor instance
     */
    public MessageFrameHandler(IWebSocketMessageProcessor messageProcessor) {
        super(messageProcessor);
    }
    
    @Override
    protected void processMessage(WebSocketMessageDTO dto) {
        if (dto.getMessage() == null) {
            logger.warn("Received message with null content");
            return;
        }
        
        logger.info("Processing message: chatId={}, content={}, senderId={}",
                dto.getChatId(),
                dto.getMessage().getContent(),
                dto.getUser() != null ? dto.getUser().getId() : "unknown");
        
        // Process the chat message with the specialized method
        messageProcessor.processChatMessage(dto);
    }
} 