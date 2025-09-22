package com.ap4.client.websocket.frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import com.ap4.common.dto.WebSocketMessageDTO;

/**
 * Specialized frame handler for leave chat events
 */
public class LeaveChatFrameHandler extends ChatFrameHandler {
    private static final Logger logger = LogManager.getLogger(LeaveChatFrameHandler.class);
    
    /**
     * Constructor
     * 
     * @param messageProcessor The WebSocketMessageProcessor instance
     */
    public LeaveChatFrameHandler(IWebSocketMessageProcessor messageProcessor) {
        super(messageProcessor);
    }
    
    @Override
    protected void processMessage(WebSocketMessageDTO dto) {
        logger.info("Processing leave chat event for chat {}, user: {}", 
                dto.getChatId(), 
                dto.getUser() != null ? dto.getUser().getUsername() : "unknown");
        
        // Process the leave message with the specialized method
        messageProcessor.processLeaveChatEvent(dto);
    }
} 