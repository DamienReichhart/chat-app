package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IContactService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.ContactAddScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.ContactService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.Chat;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for adding new contacts.
 * Manages the creation of direct chats with other users.
 */
public class ContactAddController extends Controller {
    private static final Logger logger = LogManager.getLogger(ContactAddController.class);
    
    @FXML
    public Circle contactAvatar;
    
    @FXML
    public TextField contactNameField;
    
    @FXML
    public Button addButton;
    
    private IContactService contactService;
    private IUserService userService;

    /**
     * Default constructor
     */
    public ContactAddController() {
        super();
        this.contactService = new ContactService();
        this.userService = new UserService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public ContactAddController(IContactService contactService, IUserService userService) {
        super();
        this.contactService = contactService;
        this.userService = userService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to initialize
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        this.addContactAction();
    }

    /**
     * Handle adding a new contact
     */
    private void addContactAction() {
        String contactName = contactNameField.getText();
        if (contactName.isEmpty()) {
            this.showError("Please enter a valid contact name");
            return;
        }
        
        try {
            // Get current user
            User currentUser = this.sessionService.getCurrentUser();
            if (currentUser == null) {
                logger.error("Current user is null");
                showError("You must be logged in to add a contact");
                return;
            }
            
            // Find the user to add as contact
            User contactUser = userService.getUserByUsername(contactName);
            if (contactUser == null) {
                this.showError("User '" + contactName + "' does not exist");
                return;
            }
            
            // Create direct chat with the contact user
            Chat directChat = contactService.createDirectChat(currentUser, contactUser);
            logger.info("Direct chat created with user: {}", contactUser.getUsername());
            
            // Return to main scene
            this.reloadScene(MainScene.class);
            this.closePopUp(ContactAddScene.class);
        } catch (IllegalArgumentException e) {
            logger.warn("Contact creation failed: {}", e.getMessage());
            showError(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding contact", e);
            showError("An error occurred while adding the contact");
        }
    }

    @Override
    public void reload() {
        // Clear form fields
        contactNameField.clear();
    }
}
