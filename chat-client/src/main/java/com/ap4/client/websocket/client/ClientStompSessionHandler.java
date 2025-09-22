package com.ap4.client.websocket.client;

import com.ap4.client.interfaces.websocket.IWebSocketHandler;
import com.ap4.client.websocket.WebSocketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

/**
 * Handler for STOMP session events
 */
public class ClientStompSessionHandler extends StompSessionHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(ClientStompSessionHandler.class);
    
    private final IWebSocketHandler webSocketHandler;
    
    /**
     * Constructor
     * 
     * @param webSocketHandler The WebSocketHandler instance
     */
    public ClientStompSessionHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("Connected to WebSocket server");
        webSocketHandler.handleSuccessfulConnection(session);
    }
    
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Error handling STOMP command {}: {}", command, exception.getMessage(), exception);
        webSocketHandler.notifyError("Error handling STOMP command: " + exception.getMessage());
    }
    
    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error("Transport error: {}", exception.getMessage(), exception);
        webSocketHandler.handleTransportError(session, exception);
    }
} 