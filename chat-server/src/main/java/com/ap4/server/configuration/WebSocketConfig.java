package com.ap4.server.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket configuration that enables STOMP protocol for message exchange
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger logger = LogManager.getLogger(WebSocketConfig.class);

    /**
     * Configure message broker options
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker to send messages to clients on specific topic destinations
        registry.enableSimpleBroker(
            "/topic/chat",
            "/topic/chat/*/join",
            "/topic/chat/*/leave",
            "/topic/chat/*/message",
            "/topic/chat/*/pin",
            "/topic/chat/*/unpin",
            "/topic/chat/*/delete"
        );
        
        // Set prefix for messages bound for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        
        logger.info("WebSocket message broker configured with specific topic endpoints");
    }

    /**
     * Register STOMP endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        logger.info("STOMP endpoints registered");
    }
    
    /**
     * Configure WebSocket container to support larger messages
     * @return ServletServerContainerFactoryBean with configured buffer sizes
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Set buffer size to 10MB
        container.setMaxTextMessageBufferSize(1024 * 1024 * 10);
        container.setMaxBinaryMessageBufferSize(1024 * 1024 * 10);
        // 10 minutes timeout
        container.setMaxSessionIdleTimeout(600000L);
        logger.info("WebSocket container configured with 10MB buffer sizes and 10 minutes timeout");
        return container;
    }
}
