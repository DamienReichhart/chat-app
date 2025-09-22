package com.ap4.client.controllers;

import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IAuthorizationService;
import com.ap4.client.services.ChatService;
import com.ap4.client.services.ChatParticipantService;
import com.ap4.client.services.MessageService;
import com.ap4.client.services.AuthorizationService;
import com.ap4.client.scenes.SceneManager;
import com.ap4.client.scenes.ChannelEditScene;
import com.ap4.client.scenes.GroupEditScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.common.enums.ChatType;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

/**
 * Controller for managing individual chat entries in the chat list.
 * This controller handles the display and interaction for each chat item in the sidebar,
 * including displaying chat information, handling user interactions like selecting a chat,
 * and providing context menu options for chat management.
 * <p>
 * Each chat entry displays:
 * - Avatar with consistent color based on chat name
 * - Chat title (channel, group, or contact name)
 * - Last message preview
 * - Timestamp of last message
 * - Unread message counter
 * <p>
 * The controller provides functionality for chat selection, editing, and deletion
 * through direct clicks and context menu interactions.
 */
public class ChatListCellController extends Controller {
    private static final Logger logger = LogManager.getLogger(ChatListCellController.class);
    
    /**
     * Main container for the entire chat list cell.
     */
    @FXML
    public HBox mainBox;
    
    /**
     * Container for text elements (title, message, time).
     */
    @FXML
    public VBox textContainer;
    
    /**
     * Circle shape used for the avatar background.
     */
    @FXML
    private Circle avatarCircle;
    
    /**
     * Label displaying the avatar text (usually first letter of chat name).
     */
    @FXML
    private Label avatarText;
    
    /**
     * Label displaying the chat title (channel, group, or contact name).
     */
    @FXML
    private Label titleLabel;
    
    /**
     * Label displaying a preview of the last message.
     */
    @FXML
    private Label messageLabel;
    
    /**
     * Label displaying the timestamp of the last message.
     */
    @FXML
    private Label timeLabel;
    
    /**
     * Label displaying the number of unread messages.
     */
    @FXML
    private Label unreadCountLabel;

    /**
     * Context menu for additional chat operations.
     */
    private ContextMenu contextMenu = new ContextMenu();
    
    /**
     * Menu item for editing chat properties.
     */
    private MenuItem editItem = new MenuItem("Edit");
    
    /**
     * Menu item for deleting or leaving a chat.
     */
    private MenuItem deleteItem = new MenuItem("Delete");
    
    /**
     * Menu item for viewing a contact's profile.
     */
    private MenuItem viewProfileItem = new MenuItem("View Profile");
    
    /**
     * Array of predefined colors used for chat avatars.
     * These colors are assigned to chats deterministically based on the chat name.
     */
    private final Color[] AVATAR_COLORS = {
        Color.web("#FF6B81"), // Pink
        Color.web("#5DADE2"), // Blue
        Color.web("#2ECC71"), // Green
        Color.web("#F7DC6F"), // Yellow
        Color.web("#BB8FCE"), // Purple
        Color.web("#E67E22")  // Orange
    };
    
    // Service instances
    private final IChatService chatService;
    private final IChatParticipantService chatParticipantService;
    private final IMessageService messageService;
    private final IAuthorizationService authorizationService;
    
    /**
     * Default constructor with service initialization
     */
    public ChatListCellController() {
        super();
        this.chatService = new ChatService();
        this.chatParticipantService = new ChatParticipantService();
        this.messageService = new MessageService();
        this.authorizationService = new AuthorizationService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public ChatListCellController(IChatService chatService, IChatParticipantService chatParticipantService, 
                                  IMessageService messageService, IAuthorizationService authorizationService) {
        super();
        this.chatService = chatService;
        this.chatParticipantService = chatParticipantService;
        this.messageService = messageService;
        this.authorizationService = authorizationService;
    }

    /**
     * Initializes the controller with required resources and sets up UI components.
     * This method is automatically called after the FXML has been loaded.
     * It sets up default values, initializes the context menu, and configures
     * event handlers for user interactions.
     *
     * @param location  The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.sceneManager = SceneManager.getInstance();
        
        // Set default values for new elements to prevent null pointer exceptions
        if (avatarText != null) {
            avatarText.setText("T");
        }
        
        if (unreadCountLabel != null) {
            unreadCountLabel.setText("");
        }
        
        // Set up context menu actions
        editItem.setOnAction(e -> {
            this.handleEdit();
        });

        deleteItem.setOnAction(e -> {
            this.handleDelete();
        });

        viewProfileItem.setOnAction(e -> {
            logger.debug("View Profile action triggered");
        });

        contextMenu.getItems().addAll(editItem, deleteItem, viewProfileItem);

        // Set up right-click handler for context menu
        if (mainBox != null) {
            mainBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    if (titleLabel != null) {
                        contextMenu.show(titleLabel, event.getScreenX(), event.getScreenY());
                    }
                } else {
                    contextMenu.hide();
                }
            });
        }
        
        // Set up double-click handler for editing
        if (mainBox != null) {
            mainBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                        if(mouseEvent.getClickCount() == 2){
                            handleEdit();
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles the edit action for a chat.
     * Determines the chat type and opens the appropriate edit scene.
     * For CHANNEL type, opens the channel edit scene.
     * For GROUP type, opens the group edit scene.
     * For INDIVIDUAL type, displays a message that editing is not allowed.
     * This is triggered by either selecting "Edit" from the context menu
     * or double-clicking on the chat entry.
     */
    private void handleEdit() {
        try {
            if (titleLabel != null) {
                String chatName = titleLabel.getText();
                Chat chat = chatService.getChatByName(chatName);
                
                if (chat == null) {
                    logger.error("Chat not found: {}", chatName);
                    showErrorDisplay("Chat not found.");
                    return;
                }
                
                // Get the current user
                User currentUser = sessionService.getCurrentUser();
                if (currentUser == null) {
                    logger.error("Current user is null, cannot edit chat");
                    showErrorDisplay("User session expired.");
                    return;
                }
                
                // Check authorization using the service
                if (!authorizationService.canEditChat(chat, currentUser)) {
                    logger.info("User {} does not have permission to edit chat {}", currentUser.getId(), chat.getId());
                    showErrorDisplay("You don't have permission to edit this chat.");
                    return;
                }
                
                // Open appropriate edit scene based on chat type
                ChatType type = chat.getChatType();
                if (type == ChatType.GROUP) {
                    // Open group edit scene
                    this.sessionService.setElement("editingGroupId", String.valueOf(chat.getId()));
                    this.sceneManager.openPopUpScene(GroupEditScene.class);
                } else if (type == ChatType.CHANEL) {
                    // Open channel edit scene
                    this.sessionService.setElement("editingChannelId", String.valueOf(chat.getId()));
                    this.sceneManager.openPopUpScene(ChannelEditScene.class);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling edit action: {}", e.getMessage(), e);
            showErrorDisplay("Failed to edit chat.");
        }
    }

    /**
     * Handles the delete action for a chat.
     * Performs the necessary cleanup operations when a user wishes to delete
     * or leave a chat, including:
     * - Removing the user from the chat participant list
     * - Deleting the chat entirely if no participants remain
     * - Removing all associated messages if the chat is deleted
     * This is triggered by selecting "Delete" from the context menu.
     */
    private void handleDelete() {
        try {
            if (titleLabel != null) {
                String channelName = titleLabel.getText();
                logger.debug("Deletion of channel started: {}", channelName);
                
                // Get the chat object
                Chat actChat = chatService.getChatByName(channelName);
                if (actChat == null) {
                    logger.error("Chat not found: {}", channelName);
                    showErrorDisplay("Chat not found.");
                    return;
                }
                
                // Get the current user
                User currentUser = sessionService.getCurrentUser();
                if (currentUser == null) {
                    logger.error("Current user is null, cannot delete chat");
                    showErrorDisplay("User session expired.");
                    return;
                }
                
                // Check authorization using the service
                if (!authorizationService.canDeleteChat(actChat, currentUser)) {
                    logger.info("User {} does not have permission to delete chat {}", currentUser.getId(), actChat.getId());
                    showErrorDisplay("You don't have permission to delete this chat.");
                    return;
                }
                
                // Remove the current user from the chat
                chatParticipantService.removeParticipant(currentUser.getId(), actChat.getId());
                
                // For individual chats, remove both participants
                if (actChat.getChatType() == ChatType.INDIVIDUAL) {
                    List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(actChat.getId());
                    for (ChatParticipant participant : participants) {
                        chatParticipantService.removeParticipant(participant.getUser().getId(), actChat.getId());
                    }
                }

                // If no participants remain, delete the entire chat and its messages
                if (chatParticipantService.getParticipantsByChatId(actChat.getId()).isEmpty()) {
                    // Use the proper service method for message deletion
                    List<Message> messages = messageService.getMessagesByChatId(actChat.getId());
                    for (Message message : messages) {
                        messageService.markMessageAsDeleted(message.getId());
                    }
                    
                    // Mark chat as deleted
                    chatService.markChatAsDeleted(actChat.getId());
                }
                
                // Reload the main scene to reflect the changes
                this.sceneManager.reloadScene(MainScene.class);
                logger.debug("Deletion of channel finished: {}", channelName);
            }
        } catch (Exception e) {
            logger.error("Error handling delete action: {}", e.getMessage(), e);
            showErrorDisplay("Failed to delete chat.");
        }
    }

    /**
     * Sets up the avatar with the first letter of the chat name and a color.
     * The color is deterministically selected based on the chat name to ensure
     * consistent colors for the same chats across sessions.
     * 
     * @param name The name of the chat to create an avatar for
     */
    public void setAvatar(String name) {
        if (name != null && !name.isEmpty()) {
            // Get first letter and make uppercase
            String firstLetter = name.substring(0, 1).toUpperCase();
            
            if (avatarText != null) {
                avatarText.setText(firstLetter);
            }
            
            // Generate a consistent color based on the name's hash code
            int colorIndex = Math.abs(name.hashCode()) % AVATAR_COLORS.length;
            
            if (avatarCircle != null) {
                avatarCircle.setFill(AVATAR_COLORS[colorIndex]);
            }
        }
    }

    /**
     * Gets the avatar circle component.
     * 
     * @return The Circle object representing the avatar background
     */
    public Circle getAvatarCircle() {
        return avatarCircle;
    }

    /**
     * Gets the title label component.
     * 
     * @return The Label object displaying the chat title
     */
    public Label getTitleLabel() {
        return titleLabel;
    }

    /**
     * Gets the message label component.
     * 
     * @return The Label object displaying the message preview
     */
    public Label getMessageLabel() {
        return messageLabel;
    }

    /**
     * Gets the time label component.
     * 
     * @return The Label object displaying the timestamp
     */
    public Label getTimeLabel() {
        return timeLabel;
    }
    
    /**
     * Gets the unread count label component.
     * 
     * @return The Label object displaying the unread message count
     */
    public Label getUnreadCountLabel() {
        return unreadCountLabel;
    }

    /**
     * Reloads the controller's data and refreshes the UI.
     * Updates the avatar and any other time-sensitive components.
     */
    @Override
    public void reload() {
        try {
            // Get the chat name
            if (titleLabel != null && titleLabel.getText() != null) {
                String chatName = titleLabel.getText();
                
                // Set up the avatar
                setAvatar(chatName);
                
                // Any additional reload logic can go here
            }
        } catch (Exception e) {
            logger.error("Error reloading chat list cell: {}", e.getMessage(), e);
        }
    }

    /**
     * Displays an error message to the user.
     * This is a simplified error display method as this controller may not have direct access
     * to an error display label. It logs the error and could be extended to show a popup or other UI notification.
     *
     * @param message The error message to display
     */
    private void showErrorDisplay(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        logger.debug("Error in ChatListCell: {}", message);
        // In a real implementation, this could display an alert or set text to an error label
        // For now, we'll just log the error
    }
}
