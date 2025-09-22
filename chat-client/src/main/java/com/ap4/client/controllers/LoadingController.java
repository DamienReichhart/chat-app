package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.ILoadingService;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.LoadingService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Controller for the loading screen that preloads user conversations into cache.
 * This screen is displayed after successful login and before the main application view.
 * The controller now uses LoadingService to handle the actual data loading.
 */
public class LoadingController extends Controller {
    private static final Logger logger = LogManager.getLogger(LoadingController.class);
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label percentageLabel;
    
    private final ILoadingService loadingService;
    
    /**
     * Constructor initializes required services
     */
    public LoadingController() {
        super();
        this.loadingService = new LoadingService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public LoadingController(ILoadingService loadingService) {
        super();
        this.loadingService = loadingService;
    }
    
    /**
     * Initialize the controller after FXML has been loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set initial state
        progressBar.setProgress(0);
        statusLabel.setText("Initializing...");
        percentageLabel.setText("0%");
    }

    /**
     * Start the loading process
     */
    public void load() {
        try {
            // Use the loading service to load data
            loadingService.loadUserData(
                // Progress callback - updates UI with status and progress
                this::updateProgressDisplay,
                
                // Completion callback - handles success or failure
                (success, message) -> {
                    if (success) {
                        logger.info("Loading completed successfully: {}", message);
                        // Switch to main scene on success
                        Platform.runLater(() -> switchScene(MainScene.class));
                    } else {
                        logger.error("Loading failed: {}", message);
                        // Show error message for 2 seconds before proceeding
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(() -> switchScene(MainScene.class));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                Platform.runLater(() -> switchScene(MainScene.class));
                            }
                        }).start();
                    }
                }
            );
        } catch (Exception e) {
            logger.error("Error starting loading process: {}", e.getMessage(), e);
            updateProgressDisplay("Error initializing loading. Please try again.", 0);
            
            // Delay for 2 seconds to show the error message
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> switchScene(MainScene.class));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> switchScene(MainScene.class));
                }
            }).start();
        }
    }
    
    /**
     * Reload the controller data, starting the preload process.
     */
    @Override
    public void reload() {
        load();
    }
    
    /**
     * Updates the UI components to display current progress.
     * This method handles the Platform.runLater calls to update UI from background thread.
     * 
     * @param status The text to display in the status label
     * @param progress The current progress (0.0 to 1.0)
     */
    private void updateProgressDisplay(String status, double progress) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            progressBar.setProgress(progress);
            int percentage = (int) (progress * 100);
            percentageLabel.setText(percentage + "%");
            logger.debug("Progress update: {}% - {}", percentage, status);
        });
    }
}
