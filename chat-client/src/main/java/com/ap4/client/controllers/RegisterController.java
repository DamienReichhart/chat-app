package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.handler.ExceptionHandler;
import com.ap4.client.interfaces.services.IAuthenticationService;
import com.ap4.client.scenes.LoginScene;
import com.ap4.client.services.AuthenticationService;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for the registration screen.
 * Handles user registration and navigation to login screen.
 */
public class RegisterController extends Controller {
    @FXML
    public Circle logoCircle;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField emailField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public Button registerButton;
    @FXML
    public Label errorAlert;
    @FXML
    public Button loginButton;
    
    private final IAuthenticationService authService;
    
    /**
     * Default constructor.
     * Initializes authentication service.
     */
    public RegisterController() {
        this.authService = new AuthenticationService();
    }

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No initialization needed as event handlers are defined with FXML
    }

    /**
     * Handles the login button click.
     * Navigates to login screen.
     */
    @FXML
    private void handleLoginButton(ActionEvent event) {
        this.loginAction();
    }

    /**
     * Handles the register button click.
     * Validates and processes registration.
     */
    @FXML
    private void handleRegisterButton(ActionEvent event) {
        this.registerAction();
    }

    /**
     * Navigates to the login screen.
     */
    private void loginAction() {
        this.switchScene(LoginScene.class);
    }

    /**
     * Performs the registration action.
     * Validates input and registers the user.
     */
    private void registerAction() {
        String username = this.usernameField.getText();
        String email = this.emailField.getText();
        String password = this.passwordField.getText();
        String confirmPassword = this.confirmPasswordField.getText();
        
        try {
            // Clear any previous error messages
            this.errorAlert.setText("");
            
            // Use authentication service to register user
            User newUser = authService.register(username, email, password, confirmPassword);
            
            // If registration successful, navigate to login screen
            this.showInfo("Registration successful! Please log in.");
            this.switchScene(LoginScene.class);
        } catch (DataValidationException e) {
            // Show validation errors in a user-friendly way
            ExceptionHandler.handleValidationException(e, "Registration failed");
            
            // Show the first validation error in the UI for immediate feedback
            if (!e.getValidationErrors().isEmpty()) {
                this.errorAlert.setText(e.getValidationErrors().get(0).getMessage());
            }
        } catch (DuplicateKeyException e) {
            // Handle duplicate key exceptions specifically
            String errorMessage = e.getMessage();
            this.errorAlert.setText(errorMessage);
            
            // Highlight the field with the duplicate value
            if (errorMessage.contains("username")) {
                this.usernameField.setStyle("-fx-border-color: red;");
            } else if (errorMessage.contains("email")) {
                this.emailField.setStyle("-fx-border-color: red;");
            }
        } catch (Exception e) {
            // Handle any other unexpected errors
            ExceptionHandler.handle(e, "An unexpected error occurred during registration");
        }
    }

    /**
     * Reloads the controller by clearing all fields.
     */
    @Override
    public void reload() {
        // Clear all fields
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        
        // Reset field styles
        usernameField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
        
        // Clear error alert
        if (errorAlert != null) {
            errorAlert.setText("");
        }
    }

    @Override
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
