package com.ap4.client.websocket.session;

import com.ap4.client.interfaces.websocket.IWebSocketConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

/**
 * Handler for STOMP session events related to connection management
 */
public class ConnectionStompSessionHandler extends StompSessionHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(ConnectionStompSessionHandler.class);
    
    private final IWebSocketConnectionManager connectionManager;
    
    /**
     * Constructor
     * 
     * @param connectionManager The WebSocketConnectionManager instance
     */
    public ConnectionStompSessionHandler(IWebSocketConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("Connected to WebSocket server");
        connectionManager.handleSuccessfulConnection(session);
    }
    
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Error handling STOMP command {}: {}", command, exception.getMessage(), exception);
    }
    
    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error("Transport error: {}", exception.getMessage(), exception);
        connectionManager.handleTransportError(session, exception);
    }
} 