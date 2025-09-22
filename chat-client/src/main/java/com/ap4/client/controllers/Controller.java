package com.ap4.client.controllers;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.config.ApplicationConfig;
import com.ap4.client.config.UIConfig;
import com.ap4.client.interfaces.controllers.IController;
import com.ap4.client.scenes.SceneManager;
import com.ap4.client.scenes.SuperScene;
import com.ap4.client.services.SessionService;
import com.ap4.client.ui.UIUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Abstract base class for all controllers in the application.
 * Provides common functionality and services for all derived controller classes,
 * including scene management, session access, and error handling.
 * All controllers in the application should extend this class to ensure consistent
 * behavior and access to shared resources.
 */
public abstract class Controller implements IController {
    // Change back to protected for backward compatibility
    protected static final Logger logger = LogManager.getLogger(Controller.class);
    
    /**
     * Scene manager instance for handling scene switching and navigation.
     */
    protected SceneManager sceneManager;
    
    /**
     * Session instance for storing and retrieving user session data.
     */
    protected SessionService sessionService;
    
    /**
     * Application configuration instance.
     */
    protected ApplicationConfig appConfig;
    
    /**
     * UI Configuration instance.
     */
    protected UIConfig uiConfig;

    /**
     * Label for displaying error messages to the user.
     * Can be injected by FXML.
     */
    @FXML
    protected Label errorAlert;

    /**
     * Default constructor for all controllers.
     * Initializes the controller with a session instance and logs creation.
     */
    public Controller() {
        // Log controller creation for debugging purposes
        logger.debug("Creating controller: {}", this.getClass().getSimpleName());
        
        // Get the singleton services
        this.sessionService = SessionService.getInstance();
    }

    /**
     * Initialize the scene manager for this controller.
     * Backwards compatibility method for existing controllers.
     * 
     * @param sceneManager The SceneManager instance
     */
    @Override
    public void initSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Initializes the controller with application services and configurations.
     * This method should be called after the controller is created and before it's used.
     * 
     * @param sceneManager The scene manager instance
     * @param appConfig The application configuration
     */
    public void initialize(SceneManager sceneManager, ApplicationConfig appConfig) {
        this.sceneManager = sceneManager;
        this.appConfig = appConfig;
        this.uiConfig = appConfig.getUiConfig();
        
        // Apply theme based on configuration
        applyTheme();
        
        logger.debug("Initialized controller: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Applies the configured theme to the controller's UI elements.
     * This method can be overridden by subclasses to customize theme application.
     */
    protected void applyTheme() {
        // Default implementation does nothing
        // Subclasses can override to apply theme to specific UI elements
    }

    /**
     * Switches to a different scene specified by the scene class.
     * 
     * @param scene The class representing the scene to switch to
     */
    protected void switchScene(Class<? extends SuperScene> scene) {
        logger.debug("Switching to scene: {}", scene.getSimpleName());
        this.sceneManager.switchScene(scene);
    }

    /**
     * Opens a pop-up scene specified by the scene class.
     * 
     * @param scene The class representing the pop-up scene to open
     */
    protected void openPopUpScene(Class<? extends SuperScene> scene) {
        logger.debug("Opening popup scene: {}", scene.getSimpleName());
        this.sceneManager.openPopUpScene(scene);
    }

    /**
     * Initializes nested controllers recursively.
     * This method recursively finds and initializes controllers that are fields of this controller.
     */
    public void initializeNestedControllers() {
        logger.debug("Initializing nested controllers for: {}", this.getClass().getSimpleName());
        
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Check if the field is a Controller
            if (Controller.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    // Get the controller instance from the field
                    Controller nestedController = (Controller) field.get(this);
                    if (nestedController != null) {
                        // Initialize nested controller with same configuration
                        nestedController.initialize(this.sceneManager, this.appConfig);
                        nestedController.initializeNestedControllers();
                        logger.debug("Initialized nested controller: {}", nestedController.getClass().getSimpleName());
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Could not access field: {}", field.getName(), e);
                }
            }
        }
    }

    /**
     * Executes a task on the JavaFX application thread.
     * If called from the JavaFX thread, executes immediately; otherwise, queues for later execution.
     * 
     * @param task The task to execute
     */
    protected void runOnUiThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
    
    /**
     * Finds a child node by its ID within a parent.
     * Useful for finding and manipulating UI elements that aren't directly injected.
     * 
     * @param <T> The type of the node to find
     * @param parent The parent node to search within
     * @param id The ID of the node to find
     * @param type The class of the node type
     * @return The found node, or null if not found
     */
    protected <T extends Node> T findNodeById(Pane parent, String id, Class<T> type) {
        for (Node node : parent.getChildren()) {
            if (id.equals(node.getId()) && type.isInstance(node)) {
                return (T) node;
            } else if (node instanceof Pane) {
                T result = findNodeById((Pane) node, id, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Displays an alert message to the user.
     * This method uses the UIUtils class to show a standardized alert.
     * 
     * @param message The message to display
     */
    protected void showAlert(String message) {
        logger.debug("Showing alert: {}", message);
        runOnUiThread(() -> UIUtils.showAlert(Alert.AlertType.WARNING, "Alert", message));
    }
    
    /**
     * Displays an error message to the user.
     * 
     * @param message The error message to display
     */
    protected void showError(String message) {
        logger.debug("Showing error: {}", message);
        runOnUiThread(() -> UIUtils.showAlert(Alert.AlertType.ERROR, "Error", message));
    }
    
    /**
     * Displays an information message to the user.
     * 
     * @param message The information message to display
     */
    protected void showInfo(String message) {
        logger.debug("Showing info: {}", message);
        runOnUiThread(() -> UIUtils.showAlert(Alert.AlertType.INFORMATION, "Information", message));
    }
    
    /**
     * Displays an alert message with a custom type and title.
     * 
     * @param alertType The type of alert (INFO, WARNING, ERROR, etc.)
     * @param title The title of the alert
     * @param message The message to display
     */
    protected void showAlert(Alert.AlertType alertType, String title, String message) {
        logger.debug("Showing alert - type: {}, title: {}, message: {}", alertType, title, message);
        runOnUiThread(() -> UIUtils.showAlert(alertType, title, message));
    }
    
    /**
     * Displays an alert dialog with confirmation buttons.
     * 
     * @param title The title of the confirmation dialog
     * @param message The message to display
     * @param onConfirm Action to perform if confirmed
     */
    protected void showConfirmation(String title, String message, Runnable onConfirm) {
        logger.debug("Showing confirmation dialog: {}", message);
        runOnUiThread(() -> UIUtils.showConfirmation(title, message, onConfirm));
    }

    /**
     * Reloads the controller's data and view elements.
     * This abstract method must be implemented by all subclasses
     * to define how they should refresh their content.
     */
    @Override
    public abstract void reload();

    /**
     * Closes a specific pop-up scene.
     * 
     * @param sceneClass The class of the scene to close
     */
    protected void closePopUp(Class<? extends SuperScene> sceneClass) {
        logger.debug("Closing popup: {}", sceneClass.getSimpleName());
        this.sceneManager.closePopUp(sceneClass);
    }
    
    /**
     * Closes a specific pop-up scene (backward compatibility method).
     * 
     * @param sceneClass The class of the scene to close
     * @deprecated Use closePopUp() instead
     */
    @Deprecated
    protected void close(Class<? extends SuperScene> sceneClass) {
        closePopUp(sceneClass);
    }

    /**
     * Reloads a specific scene identified by its class.
     * 
     * @param sceneClass The class of the scene to reload
     */
    protected void reloadScene(Class<? extends SuperScene> sceneClass) {
        logger.debug("Reloading scene: {}", sceneClass.getSimpleName());
        this.sceneManager.reloadScene(sceneClass);
    }
    
    /**
     * Called when the controller is being destroyed or no longer needed.
     * Subclasses should override this method to clean up resources.
     */
    public void cleanup() {
        // Default implementation does nothing
        logger.debug("Cleaning up controller: {}", this.getClass().getSimpleName());
    }
    
    
}
