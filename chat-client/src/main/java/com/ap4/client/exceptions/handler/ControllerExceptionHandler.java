package com.ap4.client.exceptions.handler;

import com.ap4.client.exceptions.service.ServiceUnavailableException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.security.AuthenticationFailureException;
import com.ap4.client.exceptions.security.AuthorizationException;
import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.websocket.WebSocketConnectionException;
import com.ap4.client.exceptions.websocket.WebSocketException;
import com.ap4.client.exceptions.websocket.WebSocketMessageException;
import com.ap4.client.exceptions.websocket.WebSocketSubscriptionException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.ui.UIUtils;

/**
 * Handler for controller-level exceptions that provides appropriate UI feedback.
 * <p>
 * This class extends the capabilities of the base ExceptionHandler with more
 * UI-focused handling specific to controllers.
 * </p>
 */
public class ControllerExceptionHandler {
    private static final Logger logger = LogManager.getLogger(ControllerExceptionHandler.class);
    
    /**
     * Handles data validation exceptions with appropriate UI feedback.
     * Shows validation errors in a formatted alert dialog.
     * 
     * @param e The validation exception
     * @param context The context where the exception occurred
     */
    public static void handleValidationException(DataValidationException e, String context) {
        logger.warn("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            VBox content = new VBox(5);
            content.getChildren().add(new Label(e.getMessage()));
            
            if (!e.getValidationErrors().isEmpty()) {
                StringBuilder errorDetails = new StringBuilder();
                e.getValidationErrors().forEach(error -> {
                    errorDetails.append("• ")
                              .append(error.getField())
                              .append(": ")
                              .append(error.getMessage())
                              .append("\n");
                });
                
                TextArea textArea = new TextArea(errorDetails.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefRowCount(Math.min(e.getValidationErrors().size() + 1, 5));
                
                content.getChildren().add(textArea);
                VBox.setVgrow(textArea, Priority.ALWAYS);
            }
            
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(context);
            alert.getDialogPane().setContent(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Handles authentication exceptions with appropriate UI feedback.
     * 
     * @param e The authentication exception
     * @param context The context where the exception occurred
     */
    public static void handleAuthenticationException(AuthenticationFailureException e, String context) {
        logger.warn("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.WARNING,
                "Authentication Failed",
                e.getMessage(),
                context
            );
        });
    }
    
    /**
     * Handles authorization exceptions with appropriate UI feedback.
     * 
     * @param e The authorization exception
     * @param context The context where the exception occurred
     */
    public static void handleAuthorizationException(AuthorizationException e, String context) {
        logger.warn("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.WARNING,
                "Access Denied",
                e.getMessage(),
                "You don't have permission to perform this action."
            );
        });
    }
    
    /**
     * Handles WebSocket exceptions with appropriate UI feedback.
     * 
     * @param e The WebSocket exception
     * @param context The context where the exception occurred
     */
    public static void handleWebSocketException(WebSocketException e, String context) {
        String detailedMessage = getWebSocketErrorMessage(e);
        logger.warn("{}: {}", context, detailedMessage, e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.ERROR,
                "Communication Error",
                detailedMessage,
                "There was a problem with the real-time communication."
            );
        });
    }
    
    /**
     * Handles "not found" exceptions with appropriate UI feedback.
     * 
     * @param e The exception
     * @param context The context where the exception occurred
     */
    public static void handleNotFoundException(DataAccessException e, String context) {
        logger.warn("{}: {}", context, e.getMessage());
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.INFORMATION,
                "Not Found",
                e.getMessage(),
                "The requested item could not be found."
            );
        });
    }
    
    /**
     * Handles a general exception with confirmation prompt for retry.
     * 
     * @param e The exception
     * @param context The context where the exception occurred
     * @param retryAction The action to perform if retry is selected
     * @return CompletableFuture that resolves to true if retry was selected
     */
    public static CompletableFuture<Boolean> handleWithRetry(Exception e, String context, Runnable retryAction) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(context);
            alert.setContentText(e.getMessage());
            
            // Add retry button
            ButtonType retryButton = new ButtonType("Retry");
            ButtonType cancelButton = new ButtonType("Cancel");
            alert.getButtonTypes().setAll(retryButton, cancelButton);
            
            // Show error details in expandable area
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label("Stack Trace:"), 0, 0);
            expContent.add(textArea, 0, 1);
            
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            alert.getDialogPane().setExpandableContent(expContent);
            
            Optional<ButtonType> response = alert.showAndWait();
            if (response.isPresent() && response.get() == retryButton) {
                // Execute retry action
                if (retryAction != null) {
                    retryAction.run();
                }
                result.complete(true);
            } else {
                result.complete(false);
            }
        });
        
        return result;
    }
    
    /**
     * Gets a user-friendly error message for WebSocket exceptions.
     * 
     * @param e The WebSocket exception
     * @return A user-friendly error message
     */
    private static String getWebSocketErrorMessage(WebSocketException e) {
        if (e instanceof WebSocketConnectionException) {
            WebSocketConnectionException ce = (WebSocketConnectionException) e;
            switch (ce.getConnectionState()) {
                case CONNECTION_FAILED:
                    return "Failed to connect to the server. Please check your network connection.";
                case CONNECTION_LOST:
                    return "Connection to the server was lost. The system will try to reconnect automatically.";
                case CONNECTION_REFUSED:
                    return "Connection was refused by the server. Please try again later.";
                case CONNECTION_TIMEOUT:
                    return "Connection timed out. Please check your network connection.";
                default:
                    return e.getMessage();
            }
        } else if (e instanceof WebSocketMessageException) {
            return "Failed to send or receive message: " + e.getMessage();
        } else if (e instanceof WebSocketSubscriptionException) {
            return "Failed to subscribe to updates: " + e.getMessage();
        } else {
            return e.getMessage();
        }
    }
    
    /**
     * Handles database connection exceptions with appropriate UI feedback.
     * 
     * @param e The database connection exception
     * @param context The context where the exception occurred
     */
    public static void handleDatabaseConnectionException(DatabaseConnectionException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText(context);
            alert.setContentText("Unable to connect to the database. Please check your network connection and try again.");
            
            // Add technical details to expandable section
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label("Technical Details:"), 0, 0);
            expContent.add(textArea, 0, 1);
            
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        });
    }
    
    /**
     * Handles service unavailability exceptions with appropriate UI feedback.
     * 
     * @param e The service unavailable exception
     * @param context The context where the exception occurred
     */
    public static void handleServiceUnavailableException(ServiceUnavailableException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.ERROR,
                "Service Unavailable",
                "The requested service is currently unavailable: " + e.getMessage(),
                "Please try again later."
            );
        });
    }
    
    /**
     * Handles data creation exceptions with appropriate UI feedback.
     * 
     * @param e The data creation exception
     * @param context The context where the exception occurred
     */
    public static void handleDataCreationException(DataCreationException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.ERROR,
                "Creation Error",
                e.getMessage(),
                "Unable to create the requested resource."
            );
        });
    }
    
    /**
     * Handles data update exceptions with appropriate UI feedback.
     * 
     * @param e The data update exception
     * @param context The context where the exception occurred
     */
    public static void handleDataUpdateException(DataUpdateException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.ERROR,
                "Update Error",
                e.getMessage(),
                "Unable to update the requested resource."
            );
        });
    }
    
    /**
     * Handles data deletion exceptions with appropriate UI feedback.
     * 
     * @param e The data deletion exception
     * @param context The context where the exception occurred
     */
    public static void handleDataDeletionException(DataDeletionException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.ERROR,
                "Deletion Error",
                e.getMessage(),
                "Unable to delete the requested resource."
            );
        });
    }
    
    /**
     * Handles duplicate key exceptions with appropriate UI feedback.
     * 
     * @param e The duplicate key exception
     * @param context The context where the exception occurred
     */
    public static void handleDuplicateKeyException(DuplicateKeyException e, String context) {
        logger.warn("{}: {}", context, e.getMessage(), e);
        
        Platform.runLater(() -> {
            UIUtils.showAlert(
                AlertType.WARNING,
                "Duplicate Entry",
                e.getMessage(),
                "Please use a different value and try again."
            );
        });
    }
} 