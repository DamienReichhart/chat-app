package com.ap4.client.interfaces.websocket;

import com.ap4.common.dto.WebSocketMessageDTO;

/**
 * Simple interface for processing WebSocket messages
 * Used by the ChatFrameHandler to delegate message processing
 */
public interface MessageProcessor {
    /**
     * Process a received WebSocket message
     * @param dto The WebSocket message DTO to process
     */
    void processReceivedMessage(WebSocketMessageDTO dto);
} 