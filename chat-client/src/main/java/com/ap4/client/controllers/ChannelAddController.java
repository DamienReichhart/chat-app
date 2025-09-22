package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.ChannelAddScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.ChannelService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.User;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.InvalidDataException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for adding a new channel.
 * Manages the channel creation UI and delegates business logic to the channel service.
 */
public class ChannelAddController extends Controller {
    private static final Logger logger = LogManager.getLogger(ChannelAddController.class);
    
    @FXML
    public Circle channelAvatar;
    @FXML
    public TextField channelNameField;
    @FXML
    public Button createButton;
    @FXML
    public TextField channelDescriptionField;
    
    // Service instances
    private final IChannelService channelService;
    private final IUserService userService;

    /**
     * Constructor with service initialization.
     */
    public ChannelAddController() {
        super();
        this.channelService = new ChannelService();
        this.userService = new UserService();
    }

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No initialization needed as event handlers are defined with FXML
    }

    /**
     * Handles the create button click.
     * Creates a new channel using the channel service.
     */
    @FXML
    private void handleCreateButton(ActionEvent event) {
        String channelName = this.channelNameField.getText();
        String description = this.channelDescriptionField.getText();
        
        // Basic input validation
        if (channelName == null || channelName.trim().isEmpty()) {
            this.showError("Channel name cannot be empty");
            return;
        }
        
        try {
            // Get current user
            User currentUser = userService.getUserById(Integer.parseInt(this.sessionService.getElement("userId")));
            if (currentUser == null) {
                logger.error("Current user is null, cannot create channel");
                this.showError("User session has expired. Please log in again.");
                return;
            }
            
            // Use channel service to create the channel
            channelService.createChannel(channelName, description, currentUser);
            
            logger.info("Channel created successfully: {}", channelName);
            
            // Reload main scene and close this scene
            this.sceneManager.reloadScene(MainScene.class);
            this.close(ChannelAddScene.class);
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format in session: {}", e.getMessage());
            this.showError("Session error: User ID is invalid");
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for channel creation: {}", e.getMessage());
            this.showError("Invalid data: " + e.getMessage());
        } catch (DataCreationException e) {
            logger.error("Failed to create channel: {}", e.getMessage());
            this.showError("Could not create channel: " + e.getMessage());
        } catch (DataAccessException e) {
            logger.error("Database error during channel creation: {}", e.getMessage());
            this.showError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Show error message if validation fails
            logger.warn("Validation failed for channel creation: {}", e.getMessage());
            this.showError(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating channel: {}", e.getMessage(), e);
            this.showError("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Reloads the controller by clearing all fields.
     */
    @Override
    public void reload() {
        // Reset input fields
        channelNameField.clear();
        channelDescriptionField.clear();
    }
}
