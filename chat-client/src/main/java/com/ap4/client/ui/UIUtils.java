package com.ap4.client.ui;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 * Utility class for UI-related operations.
 * Provides methods for displaying alerts, animations, and other UI utilities.
 */
public class UIUtils {
    private static final Logger logger = LogManager.getLogger(UIUtils.class);
    
    /**
     * Displays an alert dialog with the specified type, title, and content.
     * Ensures the alert is shown on the JavaFX application thread.
     * 
     * @param alertType The type of alert (INFO, WARNING, ERROR, etc.)
     * @param title The title of the alert dialog
     * @param content The main content text of the alert
     */
    public static void showAlert(Alert.AlertType alertType, String title, String content) {
        // Log the alert message
        switch (alertType) {
            case ERROR:
                logger.error(content);
                break;
            case WARNING:
                logger.warn(content);
                break;
            default:
                logger.info(content);
                break;
        }
        
        // Ensure alert is shown on the JavaFX application thread
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null); // No header text for cleaner look
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Displays an alert dialog with the specified type, title, header, and content.
     * Ensures the alert is shown on the JavaFX application thread.
     * 
     * @param alertType The type of alert (INFO, WARNING, ERROR, etc.)
     * @param title The title of the alert dialog
     * @param header The header text to display
     * @param content The main content text of the alert
     */
    public static void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        // Log the alert message
        switch (alertType) {
            case ERROR:
                logger.error("{}: {}", header, content);
                break;
            case WARNING:
                logger.warn("{}: {}", header, content);
                break;
            default:
                logger.info("{}: {}", header, content);
                break;
        }
        
        // Ensure alert is shown on the JavaFX application thread
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Displays an error alert with a title, header, and detailed message.
     * 
     * @param title The title of the error dialog
     * @param header The header text (main error message)
     * @param content The detailed error message
     */
    public static void showError(String title, String header, String content) {
        logger.error("{}: {} - {}", title, header, content);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Creates and configures a sliding animation for a UI node.
     * 
     * @param node The UI node to animate
     * @param fromY Starting Y position
     * @param toY Ending Y position
     * @param durationMillis Duration of the animation in milliseconds
     * @return The configured animation transition
     */
    public static TranslateTransition createSlideAnimation(Node node, double fromY, double toY, double durationMillis) {
        return getSlideAnimation(node, fromY, toY, durationMillis);
    }

    /**
     * Creates a sliding animation for a UI node.
     *
     * @param node The UI node to animate
     * @param fromY Starting Y position
     * @param toY Ending Y position
     * @param durationMillis Duration of the animation in milliseconds
     * @return The configured animation transition
     */
    public static TranslateTransition getSlideAnimation(Node node, double fromY, double toY, double durationMillis) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(durationMillis), node);
        transition.setFromY(fromY);
        transition.setToY(toY);
        return transition;
    }

    /**
     * Creates a new FileChooser with filters for common media types.
     *
     * @return The configured FileChooser
     */
    public static FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");

        // Configure file filters for common media types
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        FileChooser.ExtensionFilter videoFilter =
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.wmv");
        FileChooser.ExtensionFilter allFilesFilter = new FileChooser.ExtensionFilter("All Files", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, videoFilter, allFilesFilter);
        return fileChooser;
    }

    /**
     * Displays a confirmation dialog with OK and Cancel buttons.
     * If the user clicks OK, the onConfirm action is executed.
     * 
     * @param title The title of the confirmation dialog
     * @param message The message to display
     * @param onConfirm Action to perform if confirmed
     */
    public static void showConfirmation(String title, String message, Runnable onConfirm) {
        logger.debug("Showing confirmation dialog: {}", message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }
        });
    }
}
