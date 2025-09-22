package com.ap4.client.exceptions.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.ap4.client.exceptions.data.*;
import com.ap4.client.exceptions.db.*;
import com.ap4.client.exceptions.security.AuthenticationFailureException;
import com.ap4.client.exceptions.security.AuthorizationException;
import com.ap4.client.exceptions.service.ServiceUnavailableException;
import com.ap4.client.exceptions.session.NotASessionElementException;
import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.validation.UserNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.ui.UIUtils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Central exception handler for the application.
 * Provides methods for handling different types of exceptions in a consistent way.
 */
public class ExceptionHandler {
    private static final Logger logger = LogManager.getLogger(ExceptionHandler.class);
    
    /**
     * Handles an exception by logging it and showing an appropriate alert.
     * 
     * @param e The exception to handle
     * @param context Additional context information about where the exception occurred
     */
    public static void handle(Throwable e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        String errorMessage = getErrorMessage(e);
        UIUtils.showAlert(Alert.AlertType.ERROR, "Error", 
                String.format("%s\n%s", context, errorMessage));
    }
    
    /**
     * Handles an exception silently by only logging it without UI alerts.
     * 
     * @param e The exception to handle
     * @param context Additional context information about where the exception occurred
     */
    public static void handleSilently(Throwable e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
    }
    
    /**
     * Wraps a runnable with exception handling.
     * 
     * @param action The action to execute
     * @param context Context message for error logging
     */
    public static void runSafe(ThrowingRunnable action, String context) {
        try {
            action.run();
        } catch (Throwable e) {
            handle(e, context);
        }
    }
    
    /**
     * Wraps a runnable with silent exception handling.
     * 
     * @param action The action to execute
     * @param context Context message for error logging
     */
    public static void runSafeSilently(ThrowingRunnable action, String context) {
        try {
            action.run();
        } catch (Throwable e) {
            handleSilently(e, context);
        }
    }
    
    /**
     * Runs an action on the JavaFX application thread with exception handling.
     * 
     * @param action The action to execute
     * @param context Context message for error logging
     */
    public static void runOnUIThreadSafe(ThrowingRunnable action, String context) {
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable e) {
                handle(e, context);
            }
        });
    }
    
    /**
     * Executes a callable action with exception handling and returns a CompletableFuture.
     * 
     * @param <T> The return type of the callable
     * @param action The callable action to execute
     * @param context Context message for error logging
     * @return A CompletableFuture that completes with the result or exceptionally
     */
    public static <T> CompletableFuture<T> supplyAsync(ThrowingSupplier<T> action, String context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return action.get();
            } catch (Throwable e) {
                handle(e, context);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Gets a user-friendly error message for different exception types.
     * 
     * @param e The exception
     * @return A user-friendly error message
     */
    private static String getErrorMessage(Throwable e) {
        if (e instanceof IOException) {
            return "A network or file access error occurred. Please check your connection.";
        } else if (e instanceof SQLException) {
            return "A database error occurred. Please try again later.";
        } else if (e instanceof SecurityException) {
            return "You don't have permission to perform this action.";
        } else if (e instanceof IllegalArgumentException) {
            return "Invalid input: " + e.getMessage();
        } else if (e instanceof NullPointerException) {
            return "A system error occurred (null reference).";
        } else if (e instanceof AuthenticationFailureException) {
            return "Authentication failed: " + e.getMessage();
        } else if (e instanceof UserNotFoundException) {
            return "User not found: " + e.getMessage();
        } else if (e instanceof ChatNotFoundException) {
            return "Chat not found: " + e.getMessage();
        } else if (e instanceof MessageNotFoundException) {
            return "Message not found: " + e.getMessage();
        } else if (e instanceof ChatParticipantNotFoundException) {
            return "Chat participant not found: " + e.getMessage();
        } else if (e instanceof DataCreationException) {
            return "Failed to create data: " + e.getMessage();
        } else if (e instanceof DataUpdateException) {
            return "Failed to update data: " + e.getMessage();
        } else if (e instanceof DataDeletionException) {
            return "Failed to delete data: " + e.getMessage();
        } else if (e instanceof NotASessionElementException) {
            return "Session error: " + e.getMessage();
        } else if (e instanceof DatabaseConnectionException) {
            return "Database connection error: " + e.getMessage();
        } else if (e instanceof QueryExecutionException) {
            return "Database query error: " + e.getMessage();
        } else if (e instanceof TransactionException) {
            return "Database transaction error: " + e.getMessage();
        } else if (e instanceof InvalidDataException) {
            return "Invalid data: " + e.getMessage();
        } else if (e instanceof DuplicateKeyException) {
            return "Duplicate entry: " + e.getMessage();
        } else if (e instanceof EntityRelationshipException) {
            return "Relationship error: " + e.getMessage();
        } else if (e instanceof AuthorizationException) {
            return "Access denied: " + e.getMessage();
        } else if (e instanceof ServiceUnavailableException) {
            return "Service unavailable: " + e.getMessage();
        } else if (e instanceof DataValidationException) {
            return "Validation failed: " + e.getMessage();
        } else {
            return e.getMessage() != null ? e.getMessage() : "An unknown error occurred.";
        }
    }
    
    /**
     * Handles a data access exception by determining the appropriate user-friendly message.
     * 
     * @param e The data access exception
     * @param context Additional context information
     */
    public static void handleDataAccessException(DataAccessException e, String context) {
        logger.error("{}: {}", context, e.getMessage(), e);
        
        String errorTitle;
        AlertType alertType = AlertType.ERROR;
        
        if (e instanceof UserNotFoundException || e instanceof ChatNotFoundException || 
            e instanceof MessageNotFoundException || e instanceof ChatParticipantNotFoundException) {
            errorTitle = "Not Found";
        } else if (e instanceof DataCreationException) {
            errorTitle = "Creation Failed";
        } else if (e instanceof DataUpdateException) {
            errorTitle = "Update Failed";
        } else if (e instanceof DataDeletionException) {
            errorTitle = "Deletion Failed";
        } else if (e instanceof DatabaseConnectionException) {
            errorTitle = "Connection Error";
        } else if (e instanceof QueryExecutionException) {
            errorTitle = "Query Error";
        } else if (e instanceof TransactionException) {
            errorTitle = "Transaction Error";
        } else if (e instanceof InvalidDataException || e instanceof DuplicateKeyException) {
            errorTitle = "Invalid Data";
            alertType = AlertType.WARNING;
        } else if (e instanceof EntityRelationshipException) {
            errorTitle = "Relationship Error";
        } else {
            errorTitle = "Data Access Error";
        }
        
        UIUtils.showAlert(alertType, errorTitle, 
                String.format("%s\n%s", context, e.getMessage()));
    }
    
    /**
     * Handles an authentication failure exception with appropriate UI feedback.
     * 
     * @param e The authentication failure exception
     * @param context Additional context information
     */
    public static void handleAuthenticationFailure(AuthenticationFailureException e, String context) {
        logger.warn("{}: {}", context, e.getMessage());
        
        UIUtils.showAlert(Alert.AlertType.WARNING, "Authentication Failed", 
                String.format("%s\n%s", context, e.getMessage()));
    }
    
    /**
     * Handles a session element exception with appropriate UI feedback.
     * 
     * @param e The session element exception
     * @param context Additional context information
     */
    public static void handleSessionElementException(NotASessionElementException e, String context) {
        logger.warn("{}: {}", context, e.getMessage());
        
        UIUtils.showAlert(Alert.AlertType.WARNING, "Session Error", 
                String.format("%s\n%s", context, e.getMessage()));
    }
    
    /**
     * Handles an authorization exception with appropriate UI feedback.
     * 
     * @param e The authorization exception
     * @param context Additional context information
     */
    public static void handleAuthorizationException(AuthorizationException e, String context) {
        logger.warn("{}: {}", context, e.getMessage());
        
        UIUtils.showAlert(Alert.AlertType.WARNING, "Access Denied", 
                String.format("%s\n%s", context, e.getMessage()));
    }
    
    /**
     * Handles a service unavailable exception with appropriate UI feedback.
     * 
     * @param e The service unavailable exception
     * @param context Additional context information
     */
    public static void handleServiceUnavailableException(ServiceUnavailableException e, String context) {
        logger.error("{}: {}", context, e.getMessage());
        
        UIUtils.showAlert(Alert.AlertType.ERROR, "Service Unavailable", 
                String.format("%s\n%s", context, e.getMessage()));
    }
    
    /**
     * Handles a validation exception with appropriate UI feedback.
     * Shows a more detailed view of validation errors.
     * 
     * @param e The validation exception
     * @param context Additional context information
     */
    public static void handleValidationException(DataValidationException e, String context) {
        logger.warn("{}: {}", context, e.getMessage());
        
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(context);
        alert.setContentText(e.getMessage());
        
        // Create expandable exception if there are multiple validation errors
        if (e.getValidationErrors().size() > 1) {
            TextArea textArea = new TextArea(e.getFormattedErrors());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);
            
            alert.getDialogPane().setExpandableContent(expContent);
        }
        
        alert.showAndWait();
    }
    
    /**
     * Functional interface for operations that can throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
    
    /**
     * Functional interface for suppliers that can throw exceptions.
     * 
     * @param <T> The return type
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
    
    /**
     * Functional interface for consumers that can throw exceptions.
     * 
     * @param <T> The input type
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;
        
        /**
         * Converts a ThrowingConsumer to a regular Consumer that handles exceptions.
         * 
         * @param context Context message for error logging
         * @return A regular Consumer that handles exceptions
         */
        default Consumer<T> handleExceptions(String context) {
            return t -> {
                try {
                    accept(t);
                } catch (Throwable e) {
                    ExceptionHandler.handle(e, context);
                }
            };
        }
    }
} 