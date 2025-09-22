package com.ap4.client.websocket.frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import com.ap4.common.dto.WebSocketMessageDTO;

/**
 * Specialized frame handler for unpin message events
 */
public class UnpinMessageFrameHandler extends ChatFrameHandler {
    private static final Logger logger = LogManager.getLogger(UnpinMessageFrameHandler.class);
    
    /**
     * Constructor
     * 
     * @param messageProcessor The WebSocketMessageProcessor instance
     */
    public UnpinMessageFrameHandler(IWebSocketMessageProcessor messageProcessor) {
        super(messageProcessor);
    }
    
    @Override
    protected void processMessage(WebSocketMessageDTO dto) {
        if (dto.getMessage() == null) {
            logger.warn("Received unpin message with null content");
            return;
        }
        
        logger.info("Processing unpin message event: chatId={}, messageId={}, userId={}",
                dto.getChatId(),
                dto.getMessage().getId(),
                dto.getUser() != null ? dto.getUser().getId() : "unknown");
        
        // Process the unpin message with the specialized method
        messageProcessor.processUnpinMessageEvent(dto);
    }
} 