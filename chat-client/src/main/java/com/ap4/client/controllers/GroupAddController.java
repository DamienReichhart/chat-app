package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IGroupService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.GroupAddScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.GroupService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.User;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.InvalidDataException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the group creation interface.
 * Handles creating new group chats with name and description.
 */
public class GroupAddController extends Controller implements Initializable {
    private static final Logger logger = LogManager.getLogger(GroupAddController.class);
    
    @FXML
    private TextField groupNameField;
    
    @FXML
    private TextField groupDescriptionField;
    
    @FXML
    private Button createButton;
    
    @FXML
    private Label errorLabel;
    
    private final IGroupService groupService;
    private final IUserService userService;
    
    /**
     * Default constructor with service initialization
     */
    public GroupAddController() {
        this.groupService = new GroupService();
        this.userService = new UserService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public GroupAddController(IGroupService groupService, IUserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear error label
        clearErrorDisplay();
    }
    
    /**
     * Handles the create button click event.
     * 
     * @param event The action event
     */
    @FXML
    private void handleCreateButton(ActionEvent event) {
        logger.debug("Create group button clicked");
        createGroupAction();
    }
    
    /**
     * Creates a new group with the provided name and description.
     * Validates input and prevents duplicate group names.
     */
    private void createGroupAction() {
        try {
            clearErrorDisplay();
            
            // Get the group name and description from input fields
            String groupName = groupNameField.getText().trim();
            String description = groupDescriptionField.getText().trim();
            
            // Basic input validation
            if (groupName == null || groupName.isEmpty()) {
                showErrorDisplay("Group name cannot be empty");
                return;
            }
            
            // Get the current user
            User currentUser = sessionService.getCurrentUser();
            if (currentUser == null) {
                logger.error("Current user is null, cannot create group");
                showErrorDisplay("User session has expired. Please log in again.");
                return;
            }
            
            // Create the group using the GroupService
            groupService.createGroup(groupName, description, currentUser);
            
            // Log success and reload the main scene
            logger.info("Group created successfully: {}", groupName);
            
            // Return to the main scene
            closePopUp(GroupAddScene.class);
            this.sceneManager.reloadScene(MainScene.class);
        } catch (InvalidDataException e) {
            // Handle validation errors
            logger.warn("Invalid data for group creation: {}", e.getMessage());
            showErrorDisplay("Invalid data: " + e.getMessage());
        } catch (DataCreationException e) {
            // Handle database creation errors
            logger.error("Failed to create group in database: {}", e.getMessage());
            showErrorDisplay("Could not create group: " + e.getMessage());
        } catch (DataAccessException e) {
            // Handle database access errors
            logger.error("Database error during group creation: {}", e.getMessage());
            showErrorDisplay("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle validation errors from service layer
            logger.warn("Failed to create group: {}", e.getMessage());
            showErrorDisplay(e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error creating group: {}", e.getMessage(), e);
            showErrorDisplay("An unexpected error occurred. Please try again later.");
        }
    }
    
    /**
     * Displays an error message on the UI.
     *
     * @param message The error message to display
     */
    private void showErrorDisplay(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
    
    /**
     * Clears the error display.
     */
    private void clearErrorDisplay() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }
    
    /**
     * Reloads the controller state, clearing inputs and errors.
     */
    @Override
    public void reload() {
        // Clear input fields
        if (groupNameField != null) {
            groupNameField.clear();
        }
        
        if (groupDescriptionField != null) {
            groupDescriptionField.clear();
        }
        
        // Clear any error messages
        clearErrorDisplay();
    }
}
