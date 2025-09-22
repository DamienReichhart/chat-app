package com.ap4.client.ui.components;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ap4.client.interfaces.controllers.handler.MessageActionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.common.enums.ContentType;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import com.ap4.client.services.FileService;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Custom ListCell implementation for displaying messages in the chat interface.
 * This class handles the rendering of different message types including:
 * - Text messages with appropriate styling for incoming/outgoing messages
 * - Image messages that display directly in the chat UI
 * - File attachments with download functionality
 * - Video files that can be saved and opened with the system's default player
 * 
 * The cell adapts its display based on message content type, sender, and chat type.
 *
 */
public class MessageListCell extends ListCell<Message> {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LogManager.getLogger(MessageListCell.class);
    
    /**
     * Reference to the action handler for message operations.
     */
    private final MessageActionHandler actionHandler;
    
    /**
     * The currently logged-in user, used to determine message direction (incoming/outgoing).
     */
    private final User currentUser;
    
    /**
     * The current active chat, used to determine display rules (e.g., showing sender names in group chats).
     */
    private final Chat currentChat;
    
    /**
     * Constructor for MessageListCell.
     * 
     * @param actionHandler The handler that manages message actions like pinning, deleting, etc.
     * @param currentUser The currently logged-in user for determining message ownership
     * @param currentChat The current active chat for context-specific rendering
     */
    public MessageListCell(MessageActionHandler actionHandler, User currentUser, Chat currentChat) {
        this.actionHandler = actionHandler;
        this.currentUser = currentUser;
        this.currentChat = currentChat;
    }

    /**
     * Updates the cell content based on the message data.
     * This method is called by the ListView to configure each visible cell.
     * It handles different message types (text, image, file) and styles them
     * appropriately based on whether they are incoming or outgoing messages.
     * 
     * @param message The message to display in this cell
     * @param empty Whether this cell represents an item in the list
     */
    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);
        
        // Clear cell if no message or empty
        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        
        // Create the main container for the message
        HBox container = new HBox(10);
        
        // Determine if message is from current user (outgoing) or another user (incoming)
        boolean isOutgoing = currentUser != null && message.getSender().getId() == currentUser.getId();
        
        // Create the message bubble that will contain the content
        VBox messageBubble = new VBox(4);
        
        // Apply different styles based on message direction
        if (isOutgoing) {
            messageBubble.getStyleClass().addAll("message-bubble", "message-outgoing");
            container.setStyle("-fx-alignment: center-right;");
        } else {
            messageBubble.getStyleClass().addAll("message-bubble", "message-incoming");
            container.setStyle("-fx-alignment: center-left;");
        }
        
        // Check if this is in the pinned messages list view
        boolean isPinnedListView = getListView().getId() != null && getListView().getId().equals("pinnedMessagesListView");
        
        // If in pinned list view, make the whole message clickable
        if (isPinnedListView) {
            container.setStyle(container.getStyle() + "; -fx-cursor: hand;");
            
            // Add click event handler to container
            container.setOnMouseClicked(event -> {
                actionHandler.scrollToMessage(message);
            });
            
            // Add tooltip
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Click to locate message in chat");
            javafx.scene.control.Tooltip.install(container, tooltip);
        }
        
        // Check if message is deleted (indicated by specific content)
        boolean isDeleted = message.getContent() != null && message.getContent().equals("[Message deleted]");
        
        // If message is deleted, show deleted message indicator and return
        if (isDeleted) {
            Text deletedText = new Text("This message was deleted");
            deletedText.getStyleClass().add("message-deleted");
            deletedText.setStyle("-fx-font-style: italic; -fx-fill: #888888;");
            messageBubble.getChildren().add(deletedText);
            
            // Add timestamp
            Label timeLabel = new Label(formatTimestamp(message.getTimestamp()));
            timeLabel.getStyleClass().add("message-time");
            
            HBox bottomRow = new HBox();
            bottomRow.setStyle("-fx-alignment: center-right;");
            bottomRow.getChildren().add(timeLabel);
            messageBubble.getChildren().add(bottomRow);
            
            container.getChildren().add(messageBubble);
            setGraphic(container);
            return;
        }
        
        // If message is pinned, add a pin icon indicator
        if (message.isPinned()) {
            HBox pinIndicator = new HBox();
            Label pinIcon = new Label("📌");
            pinIcon.getStyleClass().add("pin-icon");
            
            // If this is in the pinned messages list, make the pin icon clickable to jump to message in main chat
            if (getListView().getId() != null && getListView().getId().equals("pinnedMessagesListView")) {
                pinIcon.setStyle("-fx-cursor: hand;");
                
                // Add click event handler
                pinIcon.setOnMouseClicked(event -> {
                    actionHandler.scrollToMessage(message);
                    event.consume();
                });
                
                // Add tooltip
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Click to locate message in chat");
                javafx.scene.control.Tooltip.install(pinIcon, tooltip);
            }
            
            pinIndicator.getChildren().add(pinIcon);
            messageBubble.getChildren().add(pinIndicator);
        }
        
        // In group chats and channel chats, show sender name and avatar for incoming messages to identify who sent it
        if (!isOutgoing && (currentChat.getChatType().name().equals("GROUP") || currentChat.getChatType().name().equals("CHANEL"))) {
            HBox senderInfo = new HBox(8); // 8 pixels of spacing
            senderInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            // Check if message is anonymous (only applies to channels)
            boolean isAnonymous = message.isAnonymous() && currentChat.getChatType().name().equals("CHANEL");
            
            // Create avatar circle
            Circle avatarCircle = new Circle(15); // 15 pixel radius
            
            // Default username/avatar colors for anonymous messages
            String displayUsername = isAnonymous ? "Anonymous" : message.getSender().getUsername();
            int colorIndex = 0; // Use first color for anonymous messages
            
            // Generate a consistent color based on the username's hash code if not anonymous
            if (!isAnonymous && message.getSender() != null && message.getSender().getUsername() != null) {
                String username = message.getSender().getUsername();
                // Generate consistent color from username
                colorIndex = Math.abs(username.hashCode()) % 6; // 6 colors to choose from
            }
            
            // Some predefined colors (same as in ChatListCellController)
            javafx.scene.paint.Color[] AVATAR_COLORS = {
                javafx.scene.paint.Color.web("#777777"), // Gray (for anonymous)
                javafx.scene.paint.Color.web("#FF6B81"), // Pink
                javafx.scene.paint.Color.web("#5DADE2"), // Blue
                javafx.scene.paint.Color.web("#2ECC71"), // Green
                javafx.scene.paint.Color.web("#F7DC6F"), // Yellow
                javafx.scene.paint.Color.web("#BB8FCE"), // Purple
                javafx.scene.paint.Color.web("#E67E22")  // Orange
            };
            
            avatarCircle.setFill(AVATAR_COLORS[colorIndex]);
            
            // Add first letter of username to avatar
            Label avatarLabel = new Label(displayUsername.substring(0, 1).toUpperCase());
            avatarLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            avatarLabel.setFont(new javafx.scene.text.Font("System Bold", 12));
            
            StackPane avatarPane = new StackPane();
            avatarPane.getChildren().addAll(avatarCircle, avatarLabel);
            
            // Username in blue
            Label nameLabel = new Label(displayUsername);
            nameLabel.getStyleClass().add("chat-name");
            
            // Use gray color for anonymous messages
            String textColor = isAnonymous ? "#777777" : "#3E75DD";
            nameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");
            
            senderInfo.getChildren().addAll(avatarPane, nameLabel);
            messageBubble.getChildren().add(senderInfo);
        }
        
        // Handle different content types with specialized rendering
        if (message.getContentType() == ContentType.TEXT) {
            // For text messages, simply display the content with wrapping
            Text contentText = new Text(message.getContent());
            contentText.getStyleClass().add("message-text");
            contentText.setWrappingWidth(250); // Set max width for text wrapping
            messageBubble.getChildren().add(contentText);
        } else if (message.getContentType() == ContentType.IMAGE) {
            // For image messages, display the image inline if possible
            if (message.getFileData() != null && message.getFileData().length > 0) {
                String fileType = message.getFileType() != null ? message.getFileType().toLowerCase() : "";
                // Verify that it's actually an image format we can display
                if (fileType.endsWith("jpg") || fileType.endsWith("jpeg") || 
                    fileType.endsWith("png") || fileType.endsWith("gif") || 
                    fileType.endsWith("bmp")) {
                    try {
                        // Create an image view from the byte data
                        Image image = new Image(new ByteArrayInputStream(message.getFileData()));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(200); // Constrain width for UI display
                        imageView.setPreserveRatio(true); // Maintain aspect ratio
                        
                        // Add click handler to allow viewing full-size image
                        imageView.setOnMouseClicked(event -> {
                            try {
                                // Create temporary file and open with system viewer
                                File tempFile = FileService.createTempFile(message);
                                FileService.openFileWithDefaultProgram(tempFile);
                            } catch (IOException e) {
                                logger.error("Failed to open image", e);
                                showErrorAlert("Error", "Failed to open image: " + e.getMessage());
                            }
                        });
                        
                        messageBubble.getChildren().add(imageView);
                    } catch (Exception e) {
                        // Handle image loading errors gracefully
                        logger.error("Failed to load image from byte array", e);
                        Text errorText = new Text("Failed to load image: " + message.getContent());
                        errorText.getStyleClass().add("message-text");
                        messageBubble.getChildren().add(errorText);
                    }
                } else {
                    // If it's marked as image but isn't a displayable format, show as a file
                    createFileDownloadUI(message, messageBubble);
                }
            } else {
                // Handle case where image data is missing
                Text errorText = new Text("Image data not available");
                errorText.getStyleClass().add("message-text");
                messageBubble.getChildren().add(errorText);
            }
        } else if (message.getContentType() == ContentType.FILE) {
            // For file messages, create a UI for downloading the file
            createFileDownloadUI(message, messageBubble);
        }
        
        // Add timestamp to all messages
        Label timeLabel = new Label(formatTimestamp(message.getTimestamp()));
        timeLabel.getStyleClass().add("message-time");
        
        // Create bottom row containing time and potentially status indicators
        HBox bottomRow = new HBox();
        bottomRow.setStyle("-fx-alignment: center-right;");
        bottomRow.getChildren().add(timeLabel);
        
        // Add the bottom row to the message bubble
        messageBubble.getChildren().add(bottomRow);
        
        // Add the complete message bubble to the container
        container.getChildren().add(messageBubble);
        
        // Set the entire container as the cell's graphic
        setGraphic(container);
        
        // Create and set up context menu for the message
        setupContextMenu(message, container);
    }
    
    /**
     * Sets up the context menu for message actions.
     * Creates menu items for deleting, pinning, and unpinning messages based on user permissions.
     * 
     * @param message The message to create context menu for
     * @param container The container to attach the context menu to
     */
    private void setupContextMenu(Message message, HBox container) {
        // Create context menu
        ContextMenu contextMenu = new ContextMenu();
        
        // Create menu items for different actions
        MenuItem deleteItem = new MenuItem("Delete Message");
        MenuItem pinItem = new MenuItem("Pin Message");
        MenuItem unpinItem = new MenuItem("Unpin Message");
        
        // Add action handlers
        deleteItem.setOnAction(event -> {
            actionHandler.deleteMessage(message);
        });
        
        pinItem.setOnAction(event -> {
            actionHandler.pinMessage(message);
        });
        
        unpinItem.setOnAction(event -> {
            actionHandler.unpinMessage(message);
        });
        
        // Only add relevant menu items based on state
        // Check if user can delete this message (sender or admin)
        boolean canDelete = currentUser != null && 
                           (message.getSender().getId() == currentUser.getId() || 
                           isAdmin(currentUser.getId(), currentChat.getId()));
        
        // Only show delete option for messages the user can delete
        if (canDelete) {
            contextMenu.getItems().add(deleteItem);
        }
        
        // Add pin/unpin options based on current pin state
        if (message.isPinned()) {
            contextMenu.getItems().add(unpinItem);
        } else {
            contextMenu.getItems().add(pinItem);
        }
        
        // Attach context menu to the message container if it has any items
        if (!contextMenu.getItems().isEmpty()) {
            container.setOnContextMenuRequested(event -> {
                contextMenu.show(container, event.getScreenX(), event.getScreenY());
            });
        }
    }
    
    /**
     * Creates UI elements for file download functionality.
     * This method builds a UI component that shows the filename, file type icon,
     * and a download button that allows users to save the file to their local system.
     * 
     * @param message The message containing the file data to download
     * @param messageBubble The parent container to add the file UI components to
     */
    private void createFileDownloadUI(Message message, VBox messageBubble) {
        HBox fileHeader = new HBox(10);
        fileHeader.getStyleClass().add("file-header");
        
        // Create icon based on file type
        Label fileIconLabel = new Label("📄"); // Default file icon
        fileIconLabel.setStyle("-fx-font-size: 20px;");
        
        // Create a label to display the file name
        Label fileNameLabel = new Label(message.getFileName() != null ? message.getFileName() : "File");
        fileNameLabel.getStyleClass().add("file-name");
        
        // Add icon and name to the header
        fileHeader.getChildren().addAll(fileIconLabel, fileNameLabel);
        
        // Create a download button
        Button downloadButton = new Button("Download");
        downloadButton.getStyleClass().add("download-button");
        
        // Add download functionality to the button with lazy loading
        downloadButton.setOnAction(event -> {
            // Get the stage for the file chooser dialog
            Stage stage = (Stage) getScene().getWindow();
            
            // Get the button for state management
            Button originalButton = (Button) event.getSource();
            String originalText = originalButton.getText();
            
            // Show loading state
            originalButton.setText("Loading...");
            originalButton.setDisable(true);
            
            // Use the FileUtils to handle the download
            FileService.downloadFile(
                message,
                stage,
                // Success callback
                () -> {
                    showInformationAlert("Success", "File downloaded successfully");
                    originalButton.setText(originalText);
                    originalButton.setDisable(false);
                    actionHandler.refreshMessages();
                },
                // Error callback
                (e) -> {
                    showErrorAlert("Error", "Failed to download file: " + e.getMessage());
                    originalButton.setText(originalText);
                    originalButton.setDisable(false);
                },
                // Progress callback (if needed)
                null
            );
        });
        
        // Add the header and download button to the message bubble
        messageBubble.getChildren().addAll(fileHeader, downloadButton);
    }
    
    /**
     * Formats a timestamp for human-readable display in the UI.
     * Currently displays only hours and minutes in 24-hour format.
     * 
     * @param timestamp The timestamp to format
     * @return Formatted timestamp string (HH:mm) or "Now" if null
     */
    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "Now";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(timestamp);
    }
    
    /**
     * Helper method to display an information alert
     *
     * @param title The alert title
     * @param message The alert message
     */
    private void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to display an error alert
     *
     * @param title The alert title
     * @param message The alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Determines if the user has admin permissions in the chat.
     * This is a placeholder. In a real implementation, you would check permissions from a service.
     * 
     * @param userId The user ID to check
     * @param chatId The chat ID to check permissions in
     * @return True if the user is an admin, false otherwise
     */
    private boolean isAdmin(int userId, int chatId) {
        // This would normally be a call to a service that checks permissions
        // For the purpose of the example, return true to allow the action
        return true;
    }
}
