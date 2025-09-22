package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.ChannelEditScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.ChannelService;
import com.ap4.client.services.ChatService;
import com.ap4.client.services.MessageService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

/**
 * Controller for editing an existing channel.
 */
public class ChannelEditController extends Controller {
    @FXML
    public Circle channelAvatar;
    @FXML
    public TextField channelNameField;
    @FXML
    public Label messageCountLabel;
    @FXML
    public Button modifyButton;
    @FXML
    public TextField channelDescriptionField;
    
    private int channelId;
    
    private IChatService chatService;
    private IMessageService messageService;
    private IChannelService channelService;
    private IUserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        this.chatService = new ChatService();
        this.messageService = new MessageService();
        this.channelService = new ChannelService();
        this.userService = new UserService();
        
        this.load();
    }

    @FXML
    private void handleModifyButton(ActionEvent event) {
        try {
            String channelName = this.channelNameField.getText();
            String description = this.channelDescriptionField.getText();
            
            // Get current user
            User currentUser = userService.getUserById(Integer.parseInt(this.sessionService.getElement("userId")));
            
            // Update the channel using the channel service
            channelService.updateChannel(channelId, channelName, description, currentUser);
            
            // Close this scene and reload main scene
            this.close(ChannelEditScene.class);
            this.reloadScene(MainScene.class);
        } catch (IllegalArgumentException | SecurityException e) {
            // Show error message if validation or permission check fails
            this.showError(e.getMessage());
        }
    }

    private void load() {
        try {
            // Get the channel ID from the session
            String channelIdStr = this.sessionService.getElement("editingChannelId");
            if (channelIdStr == null || channelIdStr.isEmpty()) {
                // Try with channel name for backward compatibility
                String channelName = this.sessionService.getElement("editingChannelName");
                if (channelName == null || channelName.isEmpty()) {
                    logger.error("No channel identified for editing");
                    this.showError("No channel selected for editing.");
                    this.close(ChannelEditScene.class);
                    return;
                }
                
                // Get channel ID from name
                Chat chat = this.chatService.getChatByName(channelName);
                if (chat != null) {
                    this.channelId = chat.getId();
                } else {
                    logger.error("Channel not found with name: {}", channelName);
                    this.showError("Channel not found.");
                    this.close(ChannelEditScene.class);
                    return;
                }
            } else {
                try {
                    this.channelId = Integer.parseInt(channelIdStr);
                } catch (NumberFormatException e) {
                    logger.error("Invalid channel ID format: {}", channelIdStr);
                    this.showError("Invalid channel identifier.");
                    this.close(ChannelEditScene.class);
                    return;
                }
            }
            
            // Clear previous values first
            this.channelNameField.setText("");
            this.channelDescriptionField.setText("");
            this.messageCountLabel.setText("0");
            
            // Load fresh data
            Chat chat = this.chatService.getChatById(this.channelId);
            if (chat != null) {
                this.channelNameField.setText(chat.getName());
                this.channelDescriptionField.setText(chat.getDescription());
                
                // Count messages for this chat
                List<Message> messages = this.messageService.getMessagesByChatId(chat.getId());
                this.messageCountLabel.setText(String.valueOf(messages.size()));
            } else {
                logger.error("Channel not found with ID: {}", this.channelId);
                this.showError("Channel not found.");
                this.close(ChannelEditScene.class);
            }
        } catch (Exception e) {
            logger.error("Error loading channel data: {}", e.getMessage());
            this.showError("Failed to load channel data: " + e.getMessage());
            this.close(ChannelEditScene.class);
        }
    }

    @Override
    public void reload() {
        this.load();
    }
}
