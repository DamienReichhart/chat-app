package com.ap4.client.websocket.frame;

import java.lang.reflect.Type;

import com.ap4.client.interfaces.websocket.IWebSocketMessageProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import com.ap4.common.dto.WebSocketMessageDTO;

/**
 * Base abstract handler for STOMP frames (messages) received from the server.
 * Specialized frame handlers extend this class for specific message types.
 */
public abstract class ChatFrameHandler implements StompFrameHandler {
    private static final Logger logger = LogManager.getLogger(ChatFrameHandler.class);
    
    protected final IWebSocketMessageProcessor messageProcessor;
    
    /**
     * Constructor
     * 
     * @param messageProcessor The WebSocketMessageProcessor instance
     */
    public ChatFrameHandler(IWebSocketMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }
    
    @Override
    public Type getPayloadType(StompHeaders headers) {
        logger.debug("Received message with headers: {}", headers);
        return WebSocketMessageDTO.class;
    }
    
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if (payload instanceof WebSocketMessageDTO) {
            WebSocketMessageDTO dto = (WebSocketMessageDTO) payload;
            logReceivedMessage(dto);
            
            // Process the message
            processMessage(dto);
        } else {
            logger.warn("Received unexpected payload type: {}", payload.getClass().getName());
        }
    }
    
    /**
     * Log details about the received message
     * @param dto The received message DTO
     */
    protected void logReceivedMessage(WebSocketMessageDTO dto) {
        logger.info("Received WebSocket message: type={}, chatId={}, messageId={}, content={}",
                dto.getChatId(),
                dto.getMessage() != null ? dto.getMessage().getId() : "null",
                dto.getMessage() != null ? dto.getMessage().getContent() : "null");
    }
    
    /**
     * Process the received message
     * @param dto The received message DTO
     */
    protected abstract void processMessage(WebSocketMessageDTO dto);
} 