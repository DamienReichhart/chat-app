package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IGroupService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.GroupEditScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.ChatParticipantService;
import com.ap4.client.services.ChatService;
import com.ap4.client.services.GroupService;
import com.ap4.client.services.MessageService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for the group editing interface.
 * Handles operations related to editing group information including
 * name, description, and viewing group statistics.
 */
public class GroupEditController extends Controller {
    @FXML
    public Circle groupAvatar;
    
    @FXML
    public TextField groupNameField;
    
    @FXML
    public Label participantCountLabel;
    
    @FXML
    public Label messageCountLabel;
    
    @FXML
    public Button modifyButton;
    
    @FXML
    public TextField groupDescriptionField;
    
    private Chat currentGroup;

    private IGroupService groupService;
    private IChatParticipantService chatParticipantService;
    private IChatService chatService;
    private IMessageService messageService;
    private IUserService userService;

    /**
     * Default constructor
     */
    public GroupEditController() {
        super();
        this.groupService = new GroupService();
        this.chatParticipantService = new ChatParticipantService();
        this.chatService = new ChatService();
        this.messageService = new MessageService();
        this.userService = new UserService();
    }
    
    /**
     * Constructor with dependency injection
     */
    public GroupEditController(IGroupService groupService, IChatParticipantService chatParticipantService, 
                             IChatService chatService, IMessageService messageService, IUserService userService) {
        super();
        this.groupService = groupService;
        this.chatParticipantService = chatParticipantService;
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    /**
     * Initializes the controller with required resources and sets up UI components.
     * This method is automatically called after the FXML has been loaded.
     * 
     * @param location  The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.load();
    }
    
    /**
     * Handles the modify button click event
     * 
     * @param event The action event
     */
    @FXML
    private void handleModifyButton(ActionEvent event) {
        this.handleEdit();
    }

    /**
     * Handles the edit action for updating group information.
     * Updates the database with the new group name and description,
     * then closes the edit scene and reloads the main scene.
     */
    private void handleEdit() {
        if (currentGroup != null) {
            try {
                // Get current user for permission check
                User currentUser = this.sessionService.getCurrentUser();
                if (currentUser == null) {
                    showError("You must be logged in to edit a group");
                    return;
                }
                
                // Get updated values from fields
                String newGroupName = this.groupNameField.getText();
                String newDescription = this.groupDescriptionField.getText();
                
                // Update the group using GroupService
                currentGroup = groupService.updateGroup(
                    currentGroup.getId(), 
                    newGroupName, 
                    newDescription, 
                    currentUser
                );
                
                logger.info("Group updated: {}", currentGroup.getName());
                
                // Close current scene and reload main scene
                this.closePopUp(GroupEditScene.class);
                this.reloadScene(MainScene.class);
            } catch (IllegalArgumentException e) {
                logger.warn("Group update failed: {}", e.getMessage());
                showError(e.getMessage());
            } catch (SecurityException e) {
                logger.warn("Permission denied: {}", e.getMessage());
                showError(e.getMessage());
            } catch (Exception e) {
                logger.error("Error updating group", e);
                showError("An error occurred while updating the group");
            }
        } else {
            logger.error("Cannot update group: not found");
            showError("Group not found");
        }
    }

    /**
     * Loads the current group information into the UI components.
     * Retrieves data from the database and updates the form fields accordingly.
     */
    final private void load() {
        try {
            // Get the group ID from the session
            String groupIdStr = null;
            String groupName = null;
            
            try {
                groupIdStr = this.sessionService.getString("editingGroupId");
            } catch (Exception e) {
                logger.debug("editingGroupId not found in session, trying name instead");
            }
            
            try {
                groupName = this.sessionService.getString("editingGroupName");
            } catch (Exception e) {
                logger.debug("editingGroupName not found in session");
            }
            
            if ((groupIdStr == null || groupIdStr.isEmpty()) && (groupName == null || groupName.isEmpty())) {
                logger.error("No group identifier provided for editing");
                showError("No group selected for editing");
                this.close(GroupEditScene.class);
                return;
            }
            
            // Clear previous values first
            this.groupNameField.setText("");
            this.groupDescriptionField.setText("");
            this.participantCountLabel.setText("0");
            this.messageCountLabel.setText("0");
            
            // Load group data
            if (groupIdStr != null && !groupIdStr.isEmpty()) {
                try {
                    int groupId = Integer.parseInt(groupIdStr);
                    // Get chat by ID
                    Chat chat = chatService.getChatById(groupId);
                    if (chat != null) {
                        this.currentGroup = chat;
                        populateFields(chat);
                    } else {
                        logger.error("Group not found with ID: {}", groupIdStr);
                        showError("Group not found");
                        this.close(GroupEditScene.class);
                        return;
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid group ID format: {}", groupIdStr);
                    showError("Invalid group identifier");
                    this.close(GroupEditScene.class);
                    return;
                }
            } else if (groupName != null && !groupName.isEmpty()) {
                // For backward compatibility - get chat by name
                Chat chat = chatService.getChatByName(groupName);
                if (chat != null) {
                    this.currentGroup = chat;
                    populateFields(chat);
                } else {
                    logger.error("Group not found with name: {}", groupName);
                    showError("Group not found");
                    this.close(GroupEditScene.class);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error loading group data: {}", e.getMessage());
            showError("Error loading group data: " + e.getMessage());
            this.close(GroupEditScene.class);
        }
    }
    
    /**
     * Populates the form fields with data from the chat object
     * 
     * @param chat The chat object containing group data
     */
    private void populateFields(Chat chat) {
        this.groupNameField.setText(chat.getName());
        this.groupDescriptionField.setText(chat.getDescription());
        
        // Count participants and messages for this chat
        List<ChatParticipant> participants = this.chatParticipantService.getParticipantsByChatId(chat.getId());
        this.participantCountLabel.setText(String.valueOf(participants.size()));
        
        List<com.ap4.common.models.Message> messages = this.messageService.getMessagesByChatId(chat.getId());
        this.messageCountLabel.setText(String.valueOf(messages.size()));
    }

    /**
     * Reloads the controller's data and refreshes the UI.
     * This method can be called from outside the controller to refresh its state.
     */
    @Override
    public void reload() {
        this.load();
    }
}
