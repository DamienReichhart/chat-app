package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.ap4.client.exceptions.security.AuthenticationFailureException;
import com.ap4.client.exceptions.handler.ControllerExceptionHandler;
import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.service.ServiceUnavailableException;
import com.ap4.client.interfaces.services.IAuthenticationService;
import com.ap4.client.scenes.LoadingScene;
import com.ap4.client.scenes.RegisterScene;
import com.ap4.client.services.AuthenticationService;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the login screen.
 * Handles user login and navigation to register screen.
 */
public class LoginController extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    public Circle logoCircle;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button loginButton;
    @FXML
    public Button registerButton;
    
    private final IAuthenticationService authService;
    
    /**
     * Default constructor.
     * Initializes authentication service.
     */
    public LoginController() {
        this.authService = new AuthenticationService();
    }

    /**
     * Initializes the controller.
     * Sets up event handlers.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up Enter key handler on password field
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.loginAction();
            }
        });
    }

    /**
     * Handles register button click.
     */
    @FXML
    private void handleRegisterButton(ActionEvent event) {
        this.registerAction();
    }

    /**
     * Handles login button click.
     */
    @FXML
    private void handleLoginButton(ActionEvent event) {
        this.loginAction();
    }

    /**
     * Navigates to the register screen.
     */
    private void registerAction() {
        this.switchScene(RegisterScene.class);
    }

    /**
     * Performs the login action.
     * Validates credentials and logs the user in.
     */
    private void loginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        try {
            // Use the authentication service to handle login
            User user = authService.login(username, password);
            
            // If login successful, clear fields and navigate to loading screen
            this.reload();
            this.switchScene(LoadingScene.class);
        } catch (DataValidationException e) {
            // Handle validation errors (empty fields, etc.)
            logger.warn("Login validation failed: {}", e.getMessage());
            ControllerExceptionHandler.handleValidationException(e, "Login Error");
        } catch (AuthenticationFailureException e) {
            // Handle authentication failures
            logger.warn("Authentication failed: {}", e.getMessage());
            ControllerExceptionHandler.handleAuthenticationException(e, "Login Failed");
        } catch (DatabaseConnectionException e) {
            // Handle database connection issues
            logger.error("Login error - Database connection failed: {}", e.getMessage(), e);
            ControllerExceptionHandler.handleDatabaseConnectionException(e, "Database Connection Error");
        } catch (ServiceUnavailableException e) {
            // Handle service unavailability
            logger.error("Login error - Service unavailable: {}", e.getMessage(), e);
            ControllerExceptionHandler.handleServiceUnavailableException(e, "Login Service");
        } catch (Exception e) {
            // Handle unexpected errors with retry option
            logger.error("Login error - Unexpected error: {}", e.getMessage(), e);
            ControllerExceptionHandler.handleWithRetry(e, "Login Error", this::loginAction);
        }
    }

    /**
     * Reloads the controller by clearing all fields.
     */
    public void reload() {
        // Clear all fields
        usernameField.clear();
        passwordField.clear();
    }
}
