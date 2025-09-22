package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IAuthenticationService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.UserEditScene;
import com.ap4.client.services.AuthenticationService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for the user profile edit screen.
 * Allows users to modify their profile information such as email and password.
 */
public class UserEditController extends Controller {
    private static final Logger logger = LogManager.getLogger(UserEditController.class);
    
    @FXML
    public Circle userAvatar;
    
    @FXML
    public Label usernameLabel;
    
    @FXML
    public TextField emailField;
    
    @FXML
    public PasswordField currentPasswordField;
    
    @FXML
    public PasswordField newPasswordField;
    
    @FXML
    public PasswordField confirmPasswordField;
    
    @FXML
    public Button saveButton;
    
    @FXML
    public Button cancelButton;
    
    @FXML
    public Label errorLabel;

    private IUserService userService;
    private IAuthenticationService authService;

    private User currentUser;

    /**
     * Default constructor
     */
    public UserEditController() {
        super();
        this.userService = new UserService();
        this.authService = new AuthenticationService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public UserEditController(IUserService userService, IAuthenticationService authService) {
        super();
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Initializes the controller.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear any previous error messages
        clearErrorDisplay();
        
        // Set initial text for the username label to avoid showing empty field
        usernameLabel.setText("Utilisateur");
        
        // Load the user's current information
        loadUserData();
    }
    
    /**
     * Loads the current user's data
     */
    private void loadUserData() {
        try {
            currentUser = sessionService.getCurrentUser();
            
            if (currentUser == null) {
                logger.error("Current user is null");
                showError("User session not found. Please log in again.");
                return;
            }
            
            usernameLabel.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
        } catch (Exception e) {
            logger.error("Error loading user data: {}", e.getMessage(), e);
            showError("Failed to load user data");
        }
    }

    /**
     * Handles the save button click event.
     * Validates the form inputs and updates the user profile if valid.
     *
     * @param event The action event
     */
    @FXML
    public void handleSave(ActionEvent event) {
        try {
            clearErrorDisplay();
            
            if (currentUser == null) {
                showError("User data not available");
                return;
            }
            
            // Check if the email was changed
            boolean emailChanged = false;
            String email = emailField.getText().trim();
            emailChanged = !email.equals(currentUser.getEmail());
    
            // Check if password fields are filled
            boolean passwordChanged = !currentPasswordField.getText().isEmpty() ||
                                     !newPasswordField.getText().isEmpty() ||
                                     !confirmPasswordField.getText().isEmpty();
            
            // Validate form based on what was changed
            if (emailChanged) {
                if (!isValidEmail(email)) {
                    showError("Invalid email address");
                    return;
                }
            }
            
            if (passwordChanged) {
                // Validate password fields
                if (currentPasswordField.getText().isEmpty()) {
                    showError("Please enter your current password");
                    return;
                }
                
                if (newPasswordField.getText().isEmpty()) {
                    showError("Please enter your new password");
                    return;
                }
                
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showError("Passwords do not match");
                    return;
                }
                
                if (authService.login(currentUser.getUsername(), currentPasswordField.getText()) == null) {
                    showError("Current password is incorrect");
                    return;
                }
            }
            
            // If nothing was changed, show a message
            if (!emailChanged && !passwordChanged) {
                showError("No changes were made");
                return;
            }
            
            // Save the changes
            saveChanges(emailChanged, passwordChanged);
        } catch (Exception e) {
            logger.error("Error saving user data: {}", e.getMessage(), e);
            showError("Failed to save changes: " + e.getMessage());
        }
    }
    
    /**
     * Saves the user's changes to email and/or password.
     *
     * @param emailChanged Whether the email was changed
     * @param passwordChanged Whether the password was changed
     */
    private void saveChanges(boolean emailChanged, boolean passwordChanged) {
        try {
            // Update user properties
            User updatedUser = new User();
            updatedUser.setId(currentUser.getId());
            updatedUser.setUsername(currentUser.getUsername());
            updatedUser.setEmail(emailField.getText().trim());
            
            if (passwordChanged) {
                // The password will be hashed by the service
                userService.updatePassword(currentUser.getId(), newPasswordField.getText());
            }
    
            // Update user information
            userService.updateUser(updatedUser);
            
            logger.info("User profile updated successfully for user: {}", currentUser.getUsername());
            
            // Close the dialog
            closePopUp(UserEditScene.class);
        } catch (Exception e) {
            logger.error("Error saving changes: {}", e.getMessage(), e);
            showError("Failed to save changes: " + e.getMessage());
        }
    }
    
    /**
     * Validates an email address format.
     *
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    /**
     * Clear the error label.
     */
    private void clearErrorDisplay() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }
    
    /**
     * Handles the cancel button click event.
     * Closes the dialog without saving changes.
     *
     * @param event The action event
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        closePopUp(UserEditScene.class);
    }

    /**
     * Reloads the controller.
     */
    @Override
    public void reload() {
        clearErrorDisplay();
        loadUserData();
    }
}
