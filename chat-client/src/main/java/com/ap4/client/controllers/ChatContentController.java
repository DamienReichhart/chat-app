package com.ap4.client.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.handler.ExceptionHandler;
import com.ap4.client.exceptions.websocket.WebSocketMessageException;
import com.ap4.client.exceptions.websocket.WebSocketSubscriptionException;
import com.ap4.client.interfaces.controllers.handler.MessageActionHandler;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.interfaces.websocket.MessageListener;
import com.ap4.client.services.ChatParticipantService;
import com.ap4.client.services.MessageService;
import com.ap4.client.services.UserService;
import com.ap4.client.services.WebSocketService;
import com.ap4.client.ui.UIUtils;
import com.ap4.client.ui.components.MessageListCell;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.ContentType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller class responsible for managing chat content display and interaction.
 */
public class ChatContentController extends Controller implements MessageListener, MessageActionHandler {
    // Logger for this class
    private static final Logger logger = LogManager.getLogger(ChatContentController.class);
    
    // UI Components
    @FXML public ListView<Message> messageListView;
    @FXML public TextField messageField;
    @FXML public Button sendButton;
    @FXML public Button attachFileButton;
    @FXML public CheckBox anonymousCheckbox;
    @FXML private HBox searchBarContainer;
    @FXML private TextField searchField;
    @FXML private HBox pinnedMessagesBanner;
    @FXML private Button closePinnedButton;
    @FXML private ListView<Message> pinnedMessagesListView;
    @FXML private VBox pinnedMessagesPanel;
    
    // Services
    private final IMessageService messageService;
    private final IChatParticipantService chatParticipantService;
    private final IUserService userService;
    private final WebSocketService webSocketService;
    
    // State
    private Chat currentChat;
    private User currentUser;
    private File selectedFile;
    private boolean pinnedMessagesPanelVisible = false;
    
    // Data
    private ObservableList<Message> messages;
    private ObservableList<Message> pinnedMessages;
    private FilteredList<Message> filteredMessages;

    /**
     * Default constructor initializing services.
     */
    public ChatContentController() {
        super();
        this.messageService = new MessageService();
        this.chatParticipantService = new ChatParticipantService();
        this.userService = new UserService();
        this.webSocketService = WebSocketService.getInstance();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public ChatContentController(
            IMessageService messageService,
            IChatParticipantService chatParticipantService,
            IUserService userService,
            WebSocketService webSocketService) {
        super();
        this.messageService = messageService;
        this.chatParticipantService = chatParticipantService;
        this.userService = userService;
        this.webSocketService = webSocketService;
    }

    /**
     * Initializes the controller with required resources and sets up UI components.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize observable lists
        messages = FXCollections.observableArrayList();
        pinnedMessages = FXCollections.observableArrayList();
        
        // Get current user from session
        this.currentUser = loadCurrentUser();
        
        // Register as WebSocket listener
        logger.info("Registering ChatContentController as WebSocket message listener");
        webSocketService.addMessageListener(this);
        
        // Set message list view items
        messageListView.setItems(messages);
        pinnedMessagesListView.setItems(pinnedMessages);
        
        // Set up search functionality
        setupSearchBar();
        setupSearchBarListener();
        
        // Set up pinned messages banner
        setupPinnedMessagesBanner();
        
        // Disable message controls until a chat is selected
        disableMessageControls();
        
        // Set up message input field listener
        messageField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean noText = newValue.trim().isEmpty();
            sendButton.setDisable(noText);
        });
    }
    
    /**
     * Sets up the search bar functionality.
     */
    private void setupSearchBar() {
        // Set the search field width to take up most of the container
        searchField.prefWidthProperty().bind(searchBarContainer.widthProperty().multiply(0.85));
    }
    
    /**
     * Sets up the pinned messages banner click event to toggle panel visibility.
     */
    private void setupPinnedMessagesBanner() {
        // Make the banner clickable to toggle panel visibility
        pinnedMessagesBanner.setOnMouseClicked(event -> {
            if (event.getTarget() != pinnedMessagesBanner.getChildren().get(1)) { // Avoid clicking on close button
                togglePinnedMessagesPanel();
            }
        });
    }
    
    /**
     * Sets up the search field listener.
     */
    private void setupSearchBarListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            performSearch(newValue);
        });
    }

    /**
     * Toggles the visibility of the pinned messages panel.
     */
    private void togglePinnedMessagesPanel() {
        pinnedMessagesPanelVisible = !pinnedMessagesPanelVisible;
        pinnedMessagesPanel.setVisible(pinnedMessagesPanelVisible);
        pinnedMessagesPanel.setManaged(pinnedMessagesPanelVisible);
    }
    
    /**
     * Updates the pinned messages banner and panel visibility.
     * 
     * @param hasPinned Whether there are pinned messages
     */
    private void updatePinnedMessagesBanner(boolean hasPinned) {
        // Update banner visibility
        pinnedMessagesBanner.setVisible(hasPinned);
        pinnedMessagesBanner.setManaged(hasPinned);
        
        if (hasPinned) {
            pinnedMessagesPanel.setVisible(pinnedMessagesPanelVisible);
            pinnedMessagesPanel.setManaged(pinnedMessagesPanelVisible);
        } else {
            // Directly hide panel if no pinned messages
            pinnedMessagesPanelVisible = false;
            pinnedMessagesPanel.setVisible(false);
            pinnedMessagesPanel.setManaged(false);
        }
    }
    
    /**
     * Toggles the visibility of the search bar with a sliding animation.
     * 
     * @param visible Whether the search bar should be visible
     */
    public void toggleSearchBarVisibility(boolean visible) {
        if (searchBarContainer == null) return;
        
        if (visible) {
            // First make it visible but keep it off-screen for animation
            searchBarContainer.setVisible(true);
            searchBarContainer.setManaged(true);
            
            // Create and play animation to slide down
            TranslateTransition slideDown = UIUtils.getSlideAnimation(searchBarContainer, -50, 0, 200);
            slideDown.play();
            
            // Focus the search field automatically for immediate typing
            searchField.requestFocus();
        } else {
            TranslateTransition slideUp = UIUtils.getSlideAnimation(searchBarContainer, 0, -50, 200);
            slideUp.setOnFinished(event -> {
                // Hide the search bar once the animation completes
                searchBarContainer.setVisible(false);
                searchBarContainer.setManaged(false);
                
                // Reset the search when closing to show all messages again
                searchField.clear();
                performSearch("");
            });
            slideUp.play();
        }
    }
    
    /**
     * Performs a search operation on the message list.
     * 
     * @param searchText The text to search for
     */
    private void performSearch(String searchText) {
        if (filteredMessages == null) return;
        
        filteredMessages.setPredicate(createSearchPredicate(searchText));
        
        // Auto-scroll to the first match if there are any matches and search is active
        if (!filteredMessages.isEmpty() && searchText != null && !searchText.isEmpty()) {
            scrollToLastMessage();
        }
    }
    
    /**
     * Creates a predicate for filtering messages based on search text.
     * 
     * @param searchText The text to search for
     * @return A predicate for filtering messages
     */
    private Predicate<Message> createSearchPredicate(String searchText) {
        return message -> {
            // If search field is empty, show all messages
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            // Convert to lowercase for case-insensitive comparison
            String lowerCaseSearch = searchText.toLowerCase();
            
            // Search based on content type
            if (message.getContentType() == ContentType.TEXT) {
                // Search within message text content
                return message.getContent().toLowerCase().contains(lowerCaseSearch);
            } else if (message.getContentType() == ContentType.FILE || 
                      message.getContentType() == ContentType.IMAGE) {
                // For files and images, search in the filename
                String filename = message.getFileName();
                if (filename != null) {
                    return filename.toLowerCase().contains(lowerCaseSearch);
                }
            }
            
            // No match found
            return false;
        };
    }
    
    /**
     * Handles the close pinned button action.
     */
    @FXML
    private void handleClosePinnedButton(ActionEvent event) {
        pinnedMessagesPanelVisible = false;
        pinnedMessagesPanel.setVisible(false);
        pinnedMessagesPanel.setManaged(false);
    }

    /**
     * Handles the send button action.
     */
    @FXML
    private void handleSendButton(ActionEvent event) {
        sendMessage();
    }
    
    /**
     * Handles the attach file button action.
     */
    @FXML
    private void handleAttachFileButton(ActionEvent event) {
        chooseFile();
    }
    
    /**
     * Handles the message field action (Enter key press).
     */
    @FXML
    private void handleMessageField(ActionEvent event) {
        sendMessage();
    }
    
    /**
     * Handles the close search button action.
     */
    @FXML
    private void handleCloseSearchButton(ActionEvent event) {
        toggleSearchBarVisibility(false);
    }
    
    /**
     * Updates UI controls based on the user's permissions in the current chat.
     * 
     * @param canSendMessages Whether the user can send messages
     */
    private void updateUIForPermissions(boolean canSendMessages) {
        // Enable or disable message controls based on permission
        messageField.setDisable(!canSendMessages);
        sendButton.setDisable(!canSendMessages);
        attachFileButton.setDisable(!canSendMessages);
        
        // Set prompt text based on permissions
        if (!canSendMessages) {
            messageField.setPromptText("You don't have permission to send messages in this chat");
        } else {
            messageField.setPromptText("Type a message...");
        }
        
        // Update anonymous checkbox visibility based on chat type
        if (currentChat != null) {
            boolean isChannel = currentChat.getChatType() == ChatType.CHANEL;
            anonymousCheckbox.setVisible(isChannel);
            anonymousCheckbox.setManaged(isChannel);
            anonymousCheckbox.setSelected(false); // Reset to unchecked when changing chats
        } else {
            // Hide anonymous checkbox
            anonymousCheckbox.setVisible(false);
            anonymousCheckbox.setManaged(false);
        }
    }
    
    /**
     * Disables message input controls.
     */
    private void disableMessageControls() {
        messageField.setDisable(true);
        sendButton.setDisable(true);
        attachFileButton.setDisable(true);
        messageField.setPromptText("Select a chat to start messaging");
    }
    
    /**
     * Updates the cell factories for message list views.
     */
    private void updateCellFactories() {
        Platform.runLater(() -> {
            messageListView.setCellFactory(listView -> new MessageListCell(this, currentUser, currentChat));
            pinnedMessagesListView.setCellFactory(listView -> new MessageListCell(this, currentUser, currentChat));
        });
    }
    
    /**
     * Scrolls to the last message in the list.
     */
    private void scrollToLastMessage() {
        Platform.runLater(() -> {
            int size = messageListView.getItems().size();
            if (size > 0) {
                messageListView.scrollTo(size - 1);
            }
        });
    }
    
    /**
     * Clears the message input field and resets attachment.
     */
    private void clearMessageInput() {
        messageField.clear();
        selectedFile = null;
        updateAttachButtonText(null);
    }
    
    /**
     * Gets the current message text from the input field.
     * 
     * @return The message text
     */
    private String getMessageText() {
        return messageField.getText().trim();
    }
    
    /**
     * Checks if the anonymous checkbox is selected.
     * 
     * @return True if anonymous messaging is selected
     */
    private boolean isAnonymousSelected() {
        return anonymousCheckbox.isSelected();
    }
    
    /**
     * Updates the attach button text to reflect selected file.
     * 
     * @param fileName The name of the selected file or null to reset
     */
    private void updateAttachButtonText(String fileName) {
        if (fileName == null) {
            attachFileButton.setText("Attach");
        } else {
            attachFileButton.setText(fileName.length() > 10 ? fileName.substring(0, 10) + "..." : fileName);
        }
    }
    
    /**
     * Loads the current user from the session.
     */
    private User loadCurrentUser() {
        try {
            String userIdStr = this.sessionService.getElement("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                int userId = Integer.parseInt(userIdStr);
                return userService.getUserById(userId);
            }
        } catch (NumberFormatException e) {
            logger.error("Failed to parse user ID from session", e);
        }
        return null;
    }
    
    /**
     * Loads messages for the current chat.
     */
    private void loadMessages() {
        if (currentChat == null) {
            return;
        }
        
        // Update cell factories first to ensure the ListView is ready
        updateCellFactories();
        
        // Load messages asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                return messageService.getMessagesByChatId(currentChat.getId());
            } catch (Exception e) {
                logger.error("Error loading messages for chat " + currentChat.getId(), e);
                return null;
            }
        }).thenAccept(loadedMessages -> {
            // Always perform list operations on the JavaFX thread
            Platform.runLater(() -> {
                try {
                    // First set the items of ListView to null to prevent IndexOutOfBoundsException
                    messageListView.setItems(null);
                    
                    // Safely clear and repopulate the list
                    messages.clear();
                    
                    // Only add messages if we have them
                    if (loadedMessages != null && !loadedMessages.isEmpty()) {
                        messages.addAll(loadedMessages);
                        logger.debug("Loaded {} messages for chat {}", loadedMessages.size(), currentChat.getId());
                    } else {
                        logger.debug("No messages found for chat {}", currentChat.getId());
                    }
                    
                    // Create a new filtered list and set it to the ListView
                    filteredMessages = new FilteredList<>(messages, m -> true);
                    messageListView.setItems(filteredMessages);
                    
                    // Scroll to the last message if we have any
                    if (!messages.isEmpty()) {
                        scrollToLastMessage();
                    }
                } catch (Exception e) {
                    logger.error("Error updating message list UI", e);
                }
            });
        });
    }
    
    /**
     * Updates the pinned messages list for the current chat.
     */
    private void updatePinnedMessages() {
        if (currentChat == null) return;
        
        // Check if current chat has pinned messages
        boolean hasPinned = messageService.hasPinnedMessages(currentChat.getId());
        
        // Update UI
        updatePinnedMessagesBanner(hasPinned);
        
        // If there are pinned messages, update the list
        if (hasPinned) {
            // Load pinned messages
            List<Message> pinned = messageService.getPinnedMessagesByChatId(currentChat.getId());
            
            // Update list on UI thread
            Platform.runLater(() -> {
                pinnedMessagesListView.setItems(null);
                pinnedMessages.clear();
                pinnedMessages.addAll(pinned);
                pinnedMessagesListView.setItems(pinnedMessages);
                logger.debug("Loaded {} pinned messages for chat {}", pinnedMessages.size(), currentChat.getId());
            });
        }
    }
    
    /**
     * Sets the chat for this controller.
     * Loads messages, sets up permissions, and joins the WebSocket topic.
     */
    public void setChat(Chat chat) {
        if (chat == null) {
            return;
        }
        
        logger.debug("Setting chat: {} (ID: {})", chat.getName(), chat.getId());
        
        // Store chat
        this.currentChat = chat;
        
        try {
            // Check if user is a participant and get permission information
            boolean canSendMessages = chatParticipantService.hasPermission(
                    currentUser.getId(), 
                    currentChat.getId(),
                    currentChat.getChatType().name().equals("CHANEL") ? Role.ADMIN : Role.MEMBER);
            
            // Update UI for user permissions
            updateUIForPermissions(canSendMessages);
            
            // Subscribe to WebSocket topic for this chat
            if (webSocketService.isConnected()) {
                webSocketService.joinChat(currentChat);
            } else {
                logger.warn("WebSocket service is not running, cannot subscribe to chat {}", chat.getId());
            }
            
            // Reset search
            if (searchField != null) {
                searchField.clear();
            }
            
            // Reset file selection
            selectedFile = null;
            updateAttachButtonText(null);
            
            // Temporarily set ListView to null before changing the filtered list
            messageListView.setItems(null);
            
            // Set up filtered messages
            filteredMessages = new FilteredList<>(messages, m -> true);
            messageListView.setItems(filteredMessages);
            
            // Load messages for this chat
            loadMessages();
            
            // Check for pinned messages
            updatePinnedMessages();
            
        } catch (WebSocketSubscriptionException e) {
            logger.error("Failed to subscribe to chat WebSocket topic", e);
            ExceptionHandler.handle(e, "Failed to connect to chat");
        }
    }
    
    /**
     * Opens a file chooser dialog to select a file for attachment.
     */
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            selectedFile = file;
            updateAttachButtonText(file.getName());
        }
    }
    
    /**
     * Sends a message from the current user in the current chat.
     */
    private void sendMessage() {
        String messageText = getMessageText();
        boolean isAnonymous = isAnonymousSelected();
        
        // Don't send empty messages unless they have an attachment
        if (messageText.isEmpty() && selectedFile == null) {
            return;
        }
        
        try {
            // Create a message object with all common properties
            Message message = new Message();
            message.setContent(messageText);
            message.setPinned(false);
            message.setAnonymous(isAnonymous);
            message.setChat(currentChat);
            message.setSender(currentUser);
            
            // If a file is selected, add file-specific properties
            if (selectedFile != null) {
                // Determine content type based on file extension
                String fileName = selectedFile.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                
                boolean isImage = fileExtension.matches("jpg|jpeg|png|gif|bmp");
                message.setContentType(isImage ? ContentType.IMAGE : ContentType.FILE);
                message.setFileName(fileName);
                
                // Send message with file
                messageService.createFileMessageWithData(selectedFile, message);
            } else {
                // Plain text message
                message.setContentType(ContentType.TEXT);
                messageService.createMessage(message);
            }
            
            // Reset UI
            clearMessageInput();
            
            try {
                // Send WebSocket notification to all chat participants
                webSocketService.sendMessage(message);
                
                logger.info("Message sent to chat {}: {}", currentChat.getId(), 
                            message.getContentType() == ContentType.TEXT ? message.getContent() 
                                                                         : "FILE: " + message.getFileName());
            } catch (WebSocketMessageException e) {
                logger.error("Failed to send WebSocket notification", e);
                // Continue execution - the message was saved, only the real-time notification failed
            }
            
        } catch (DataCreationException e) {
            logger.error("Failed to create message", e);
            ExceptionHandler.handle(e, "Failed to send message");
        } catch (Exception e) {
            logger.error("Failed to process message", e);
            ExceptionHandler.handle(new DataCreationException("Failed to process message", e), "Message Error");
        }
    }

    /**
     * Processes a newly received message.
     * 
     * @param message The received message
     */
    private void processReceivedMessage(Message message) {
        if (message == null || currentChat == null) {
            return;
        }
        
        // Only process messages for the current chat
        if (message.getChat().getId() != currentChat.getId()) {
            return;
        }
        
        // Update the message list on the UI thread
        Platform.runLater(() -> {
            try {
                // Check if the message is deleted
                if (message.isDeleted()) {
                    // If it's deleted, remove it from both lists
                    messages.removeIf(m -> m.getId() == message.getId());
                    pinnedMessages.removeIf(m -> m.getId() == message.getId());
                    
                    // Update the UI
                    updatePinnedMessages();
                    logger.debug("Removed deleted message with ID {} from UI", message.getId());
                    return;
                }
                
                // Temporarily detach ListView from items to prevent concurrent modification issues
                ObservableList<Message> tempItems = messageListView.getItems();
                messageListView.setItems(null);
                
                // Check if this is a new message or an update
                int existingIndex = -1;
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).getId() == message.getId()) {
                        existingIndex = i;
                        break;
                    }
                }
                
                if (existingIndex >= 0) {
                    // Update existing message
                    messages.set(existingIndex, message);
                    logger.debug("Updated message with ID {}", message.getId());
                } else {
                    // Add new message
                    messages.add(message);
                    logger.debug("Added new message with ID {}", message.getId());
                }
                
                // Reattach the ListView to its items
                if (tempItems == filteredMessages) {
                    messageListView.setItems(filteredMessages);
                } else {
                    // Create a new filtered list if needed
                    filteredMessages = new FilteredList<>(messages, m -> true);
                    messageListView.setItems(filteredMessages);
                }
                
                // Scroll to new message if a new one was added
                if (existingIndex < 0) {
                    scrollToLastMessage();
                }
                
                // Always update pinned messages when a message status changes
                updatePinnedMessages();
            } catch (Exception e) {
                logger.error("Error processing received message", e);
            }
        });
    }
    
    /**
     * Reloads message data for the current chat.
     */
    @Override
    public void reload() {
        if (currentChat != null) {
            loadMessages();
            updatePinnedMessages();
        }
    }
    
    /**
     * Handles a message received from the WebSocket.
     */
    @Override
    public void onMessageReceived(Message message) {
        processReceivedMessage(message);
    }
    
    /**
     * Cleans up resources and unsubscribes from WebSocket topics.
     */
    @Override
    public void cleanup() {
        // Unsubscribe from WebSocket topic
        if (currentChat != null && webSocketService.isConnected()) {
            try {
                webSocketService.leaveChat(currentChat);
            } catch (WebSocketSubscriptionException e) {
                logger.error("Failed to unsubscribe from chat WebSocket topic", e);
            }
        }
        
        // Remove as WebSocket listener
        webSocketService.removeMessageListener(this);
    }
    
    /**
     * Scrolls to a specific message in the list.
     * 
     * @param message The message to scroll to
     */
    @Override
    public void scrollToMessage(Message message) {
        Platform.runLater(() -> {
            messageListView.scrollTo(message);
        });
    }
    
    /**
     * Pins a message in the current chat.
     * 
     * @param message The message to pin
     */
    @Override
    public void pinMessage(Message message) {
        if (message == null || message.isPinned()) {
            return;
        }
        
        try {
            // Update message pin status
            message.setPinned(true);
            messageService.updateMessage(message);
            
            // Update UI
            updatePinnedMessages();
            
            // Send notification
            webSocketService.pinMessage(message);
            
            logger.info("Pinned message with ID {}", message.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to pin message", e);
            ExceptionHandler.handle(e, "Failed to pin message");
        } catch (WebSocketMessageException e) {
            logger.error("Failed to send WebSocket notification after pinning message", e);
            // Continue execution - the message was pinned, only the real-time notification failed
        }
    }
    
    /**
     * Unpins a message in the current chat.
     * 
     * @param message The message to unpin
     */
    @Override
    public void unpinMessage(Message message) {
        if (message == null || !message.isPinned()) {
            return;
        }
        
        try {
            // Update message pin status
            message.setPinned(false);
            messageService.updateMessage(message);
            
            // Update UI
            updatePinnedMessages();
            
            // Send notification
            webSocketService.unpinMessage(message);
            
            logger.info("Unpinned message with ID {}", message.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to unpin message", e);
            ExceptionHandler.handle(e, "Failed to unpin message");
        } catch (WebSocketMessageException e) {
            logger.error("Failed to send WebSocket notification after unpinning message", e);
            // Continue execution - the message was unpinned, only the real-time notification failed
        }
    }
    
    /**
     * Deletes a message from the current chat.
     * 
     * @param message The message to delete
     */
    @Override
    public void deleteMessage(Message message) {
        if (message == null) {
            return;
        }
        
        // Confirm deletion
        boolean confirmed = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this message?");
        alert.setContentText("This action cannot be undone.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        if (result != ButtonType.YES) {
            return;
        }
        
        try {
            // Delete the message
            messageService.deleteMessage(message.getId());
            
            // Update UI
            Platform.runLater(() -> {
                messages.removeIf(m -> m.getId() == message.getId());
                pinnedMessages.removeIf(m -> m.getId() == message.getId());
                logger.debug("Removed message with ID {} from UI", message.getId());
            });
            
            // Send notification
            webSocketService.deleteMessage(message);
            
            logger.info("Deleted message with ID {}", message.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to delete message", e);
            ExceptionHandler.handle(e, "Failed to delete message");
        } catch (WebSocketMessageException e) {
            logger.error("Failed to send WebSocket notification after deleting message", e);
            // Continue execution - the message was deleted, only the real-time notification failed
        }
    }
    
    /**
     * Refreshes the messages in the current chat.
     */
    @Override
    public void refreshMessages() {
        reload();
    }
}
