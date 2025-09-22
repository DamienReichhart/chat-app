package com.ap4.client.websocket.connection;

import com.ap4.client.config.WebSocketConfig;
import com.ap4.client.exceptions.service.ServiceUnavailableException;
import com.ap4.client.exceptions.websocket.WebSocketConnectionException;
import com.ap4.client.interfaces.websocket.ConnectionListener;
import com.ap4.client.interfaces.websocket.IWebSocketConnectionManager;
import com.ap4.client.websocket.session.ConnectionStompSessionHandler;
import com.ap4.common.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages WebSocket connections with the server using STOMP protocol
 */
public class WebSocketConnectionManager implements IWebSocketConnectionManager {
    private static final Logger logger = LogManager.getLogger(WebSocketConnectionManager.class);
    private static IWebSocketConnectionManager instance;
    
    private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    
    private User currentUser;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    
    private boolean connected = false;
    private String serverUrl;
    private boolean reconnecting = false;
    private ScheduledExecutorService reconnectExecutor;
    private WebSocketConfig config;

    /**
     * Private constructor for singleton
     */
    private WebSocketConnectionManager() {
        logger.info("Initializing WebSocketConnectionManager");
        
        // Initialize with defaults, actual configuration will be set via initialize()
        try {
            Properties properties = new Properties();
            properties.load(WebSocketConnectionManager.class.getClassLoader().getResourceAsStream("websocket.properties"));
            this.serverUrl = properties.getProperty("websocket.url");
            
            // Initialize STOMP client
            setupStompClient();
            
            // Initialize reconnect executor
            this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        } catch (IOException e) {
            logger.error("Failed to load websocket properties", e);
            throw new ServiceUnavailableException("WebSocket", "Failed to load websocket properties", e);
        }
    }
    
    /**
     * Get singleton instance
     * @return WebSocketConnectionManager instance
     */
    public static synchronized IWebSocketConnectionManager getInstance() {
        if (instance == null) {
            instance = new WebSocketConnectionManager();
        }
        return instance;
    }
    
    @Override
    public void initialize(WebSocketConfig config) {
        this.config = config;
        this.serverUrl = config.getServerUrl();
        
        // Reinitialize client with new configuration
        setupStompClient();
        
        logger.info("WebSocketConnectionManager initialized with server URL: {}", serverUrl);
    }
    
    /**
     * Set up the STOMP client with appropriate configuration
     */
    private void setupStompClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        
        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
    }
    
    @Override
    public void connect(User user) {
        if (connected) {
            logger.info("Already connected to WebSocket server");
            return;
        }
        
        this.currentUser = user;
        
        try {
            logger.info("Connecting to WebSocket server at {}", serverUrl);
            
            // Create a session handler
            ConnectionStompSessionHandler sessionHandler = new ConnectionStompSessionHandler(this);
            
            // Connect and join a session
            stompClient.connectAsync(serverUrl, sessionHandler);
            
        } catch (Exception e) {
            logger.error("Error connecting to WebSocket server: {}", e.getMessage(), e);
            WebSocketConnectionException connectionException = new WebSocketConnectionException(
                WebSocketConnectionException.ConnectionState.CONNECTION_FAILED,
                "Failed to connect to chat server: " + e.getMessage(),
                e
            );
            notifyConnectionStatus(false);
            scheduleReconnect();
            throw connectionException;
        }
    }
    
    /**
     * Schedule reconnection attempt
     */
    private void scheduleReconnect() {
        if (!reconnecting && currentUser != null) {
            reconnecting = true;
            
            // Get reconnect delay from config if available
            int reconnectDelay = (config != null) ? 
                config.getReconnectAttempts() * 1000 : 5000;
                
            reconnectExecutor.schedule(() -> {
                logger.info("Attempting to reconnect to WebSocket server...");
                try {
                    setupStompClient();
                    ConnectionStompSessionHandler sessionHandler = new ConnectionStompSessionHandler(this);
                    stompClient.connectAsync(serverUrl, sessionHandler);
                } catch (Exception e) {
                    logger.error("Reconnection attempt failed: {}", e.getMessage(), e);
                    scheduleReconnect();
                }
                reconnecting = false;
            }, reconnectDelay, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void handleSuccessfulConnection(StompSession session) {
        this.stompSession = session;
        this.connected = true;
        
        // Notify listeners of connection
        notifyConnectionStatus(true);
        
        logger.info("Successfully connected to WebSocket server");
    }
    
    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        // Mark as disconnected
        if (connected) {
            this.connected = false;
            notifyConnectionStatus(false);
        }
        
        // Attempt to reconnect
        if (exception.getMessage().contains("Connection refused") || 
            exception.getMessage().contains("Connection reset")) {
            logger.warn("Connection to server lost: {}", exception.getMessage());
            scheduleReconnect();
        }
    }
    
    @Override
    public void disconnect() {
        if (!connected || stompSession == null) {
            logger.info("Not connected to WebSocket server");
            return;
        }
        
        try {
            logger.info("Disconnecting from WebSocket server");
            stompSession.disconnect();
            stompSession = null;
            connected = false;
            
            // Notify listeners
            notifyConnectionStatus(false);
        } catch (Exception e) {
            logger.error("Error disconnecting from WebSocket server: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void reset() {
        logger.info("Resetting WebSocketConnectionManager state");
        
        // Notify listeners about disconnection if they haven't been notified
        if (connected) {
            notifyConnectionStatus(false);
        }
        
        // Reset connection state
        this.stompSession = null;
        this.connected = false;
        this.currentUser = null;
    }
    
    @Override
    public boolean isConnected() {
        return connected && stompSession != null && stompSession.isConnected();
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    @Override
    public StompSession getStompSession() {
        return stompSession;
    }
    
    @Override
    public void addConnectionListener(ConnectionListener listener) {
        if (listener != null && !connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
            
            // If already connected, notify the new listener
            if (connected) {
                try {
                    listener.onConnectionStatusChanged(true);
                } catch (Exception e) {
                    logger.error("Error notifying new connection listener: {}", e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    public void removeConnectionListener(ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.remove(listener);
        }
    }
    
    @Override
    public void notifyConnectionStatus(boolean connected) {
        for (ConnectionListener listener : connectionListeners) {
            try {
                listener.onConnectionStatusChanged(connected);
            } catch (Exception e) {
                logger.error("Error notifying connection listener: {}", e.getMessage(), e);
            }
        }
    }
} 