package com.ap4.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ap4.client.services.SessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.config.ApplicationConfig;
import com.ap4.client.db.DatabaseManager;
import com.ap4.client.scenes.LoginScene;
import com.ap4.client.scenes.SceneManager;
import com.ap4.client.services.WebSocketService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main application class for the Chat Client application.
 * Extends JavaFX Application class to provide the entry point for the JavaFX GUI.
 * Responsible for initializing core components, managing application lifecycle,
 * and ensuring proper resource cleanup on shutdown.
 */
public class ChatClientApplication extends Application {
    /**
     * Logger instance for the application class.
     * Used for logging application lifecycle events and error conditions.
     */
    private static final Logger logger = LogManager.getLogger(ChatClientApplication.class);
    
    /**
     * Application configuration instance.
     */
    private ApplicationConfig applicationConfig;
    
    /**
     * Thread pool for background tasks.
     */
    private ExecutorService executorService;

    /**
     * JavaFX application entry point.
     * Initializes essential application components and sets up the primary stage.
     * 
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting ChatClientApplication");
            
            // Initialize thread pool for background tasks
            int threadCount = Runtime.getRuntime().availableProcessors();
            executorService = Executors.newFixedThreadPool(threadCount);
            
            // Initialize session singleton
            SessionService sessionService = SessionService.getInstance();
            
            // Load application configuration
            applicationConfig = new ApplicationConfig(sessionService);
            
            // Initialize database connection manager
            DatabaseManager dbManager = DatabaseManager.getInstance(applicationConfig.getDatabaseConfig());
            
            // Initialize the WebSocket service
            WebSocketService webSocketService = WebSocketService.getInstance();
            webSocketService.initialize(applicationConfig.getWebSocketConfig(), sessionService);
            
            // Set up scene manager for handling UI navigation
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.initialize(primaryStage, sessionService, applicationConfig, dbManager);
            
            // Configure primary stage
            primaryStage.setTitle(applicationConfig.getProperty("ui.window.title", "Chat Client"));
            primaryStage.setMinWidth(applicationConfig.getUiConfig().getMinWindowWidth());
            primaryStage.setMinHeight(applicationConfig.getUiConfig().getMinWindowHeight());
            primaryStage.setWidth(applicationConfig.getUiConfig().getWindowWidth());
            primaryStage.setHeight(applicationConfig.getUiConfig().getWindowHeight());
            
            // Set initial scene to login screen
            sceneManager.switchScene(LoginScene.class);
            
            // Set up proper application close handling
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application close requested");
                Platform.exit();
            });
            
            // Show the primary stage
            primaryStage.show();
            logger.info("Application UI initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            Platform.exit();
        }
    }
    
    /**
     * Handles application shutdown.
     * Ensures proper cleanup of resources including thread pools, cache, and database connections.
     * This method is automatically called by the JavaFX platform when the application is shutting down.
     * 
     * @throws Exception If an error occurs during shutdown
     */
    @Override
    public void stop() throws Exception {
        logger.info("Shutting down application...");
        
        try {
            // Clean up WebSocket connection - add null check and try/catch
            try {
                WebSocketService webSocketService = WebSocketService.getInstance();
                if (webSocketService != null) {
                    webSocketService.disconnect();
                }
            } catch (Exception e) {
                logger.error("Error disconnecting WebSocket service", e);
            }

            // Close database connection
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                if (dbManager != null) {
                    dbManager.closeAllConnections();
                }
            } catch (Exception e) {
                logger.error("Error closing database connections", e);
            }
            
            // Shutdown executor service
            shutdownExecutorService(executorService, "Application Thread Pool");
            
            logger.info("Application shutdown complete");
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
            throw e;
        } finally {
            super.stop();
        }
    }
    
    /**
     * Utility method to gracefully shutdown an ExecutorService.
     * Attempts a graceful shutdown first, then forces shutdown if tasks don't complete in time.
     * 
     * @param executorService The executor service to shut down
     * @param serviceName A descriptive name for the service (used in log messages)
     */
    private void shutdownExecutorService(ExecutorService executorService, String serviceName) {
        if (executorService != null && !executorService.isShutdown()) {
            try {
                logger.info("Shutting down {}...", serviceName);
                // Request orderly shutdown
                executorService.shutdown();
                // Wait for tasks to complete
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("{} did not terminate in time, forcing shutdown", serviceName);
                    // Force shutdown if tasks don't complete in time
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Shutdown of {} was interrupted", serviceName, e);
                // Restore interrupted status
                Thread.currentThread().interrupt();
                // Force shutdown
                executorService.shutdownNow();
            }
        }
    }

    /**
     * Application main method.
     * Entry point for the JavaFX application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
