package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatPermissionService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.ChannelMembersScene;
import com.ap4.client.services.ChannelService;
import com.ap4.client.services.ChatParticipantService;
import com.ap4.client.services.ChatPermissionService;
import com.ap4.client.services.ChatService;
import com.ap4.client.services.UserService;
import com.ap4.client.ui.components.MemberListCell;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Controller for the channel members management UI.
 * Handles displaying members and updating their roles.
 */
public class ChannelMembersController extends Controller {
    private static final Logger logger = LogManager.getLogger(ChannelMembersController.class);

    // FXML injected components
    @FXML private Label channelNameLabel;
    @FXML private TextField searchField;
    @FXML private ListView<ChatParticipant> membersListView;
    @FXML private Label errorLabel;
    @FXML private Text permissionText;
    
    // Model
    private Chat currentChat;
    private User currentUser;
    private ObservableList<ChatParticipant> members;
    private FilteredList<ChatParticipant> filteredMembers;
    private boolean canEditRoles;

    // Services
    private final IChannelService channelService;
    private final IUserService userService;
    private final IChatParticipantService chatParticipantService;
    private final IChatService chatService;
    private final IChatPermissionService chatPermissionService;
    
    /**
     * Constructor initializing the required services.
     * Uses dependency injection for services.
     */
    public ChannelMembersController() {
        this(new ChannelService(), new UserService(), new ChatParticipantService(), new ChatService(), new ChatPermissionService());
    }
    
    /**
     * Constructor with explicit service dependencies for testing.
     * 
     * @param channelService Service for channel operations
     * @param userService Service for user operations
     * @param chatParticipantService Service for chat participant operations
     * @param chatService Service for chat operations
     * @param chatPermissionService Service for permission operations
     */
    public ChannelMembersController(
            IChannelService channelService, 
            IUserService userService,
            IChatParticipantService chatParticipantService,
            IChatService chatService,
            IChatPermissionService chatPermissionService) {
        this.channelService = channelService;
        this.userService = userService;
        this.chatParticipantService = chatParticipantService;
        this.chatService = chatService;
        this.chatPermissionService = chatPermissionService;
        
        // Initialize members and filtered members
        members = FXCollections.observableArrayList();
        filteredMembers = new FilteredList<>(members, p -> true);
    }
    
    /**
     * Initializes the controller with required resources and sets up UI components.
     * This method is automatically called after the FXML has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing ChannelMembersController");
        
        // Set up list view
        membersListView.setItems(filteredMembers);
        
        // Validate UI components
        validateUIComponents();
        
        // Setup search functionality
        setupSearch(filteredMembers);
        
        // Load user and chat sequentially to ensure proper initialization order
        CompletableFuture.supplyAsync(() -> {
            // First load the current user
            this.currentUser = loadCurrentUser();
            logger.info("Current user loaded: {}", currentUser != null ? 
                currentUser.getUsername() + " (ID: " + currentUser.getId() + ")" : "null");
            return currentUser;
        }).thenAcceptAsync(user -> {
            if (user == null) {
                logger.error("Failed to load current user, cannot proceed with chat loading");
                Platform.runLater(() -> showError("Error: Could not determine current user"));
                return;
            }
            
            // Then load the chat only after user is successfully loaded
            try {
                String chatIdStr = sessionService.getElement("currentChatId");
                if (chatIdStr == null || chatIdStr.isEmpty()) {
                    logger.error("No chat ID found in session");
                    Platform.runLater(() -> showError("Error: No active chat selected"));
                    return;
                }
                
                int chatId = Integer.parseInt(chatIdStr);
                logger.debug("Loading chat with ID: {}", chatId);
                
                Chat activeChat = chatService.getChatById(chatId);
                if (activeChat == null) {
                    logger.error("Failed to load chat with ID: {}", chatId);
                    Platform.runLater(() -> showError("Error: Could not load the selected chat"));
                    return;
                }
                
                // Set chat on UI thread
                Platform.runLater(() -> this.setChat(activeChat));
            } catch (NumberFormatException e) {
                logger.error("Invalid chat ID format in session", e);
                Platform.runLater(() -> showError("Error: Invalid chat identifier"));
            } catch (Exception e) {
                logger.error("Error loading active chat", e);
                Platform.runLater(() -> showError("Error loading active chat: " + e.getMessage()));
            }
        });
        
        logger.info("ChannelMembersController initialized");
    }
    
    /**
     * Validates that all required UI components are properly initialized.
     * Logs warnings for any missing components.
     */
    private void validateUIComponents() {
        if (channelNameLabel == null) {
            logger.warn("Channel name label is null in ChannelMembersController");
        }
        
        if (searchField == null) {
            logger.warn("Search field is null in ChannelMembersController");
        }
        
        if (membersListView == null) {
            logger.error("Members list view is null in ChannelMembersController");
        }
        
        if (errorLabel == null) {
            logger.warn("Error label is null in ChannelMembersController");
        }
        
        if (permissionText == null) {
            logger.warn("Permission text is null in ChannelMembersController");
        }
    }
    
    /**
     * Sets up the search functionality for filtering members.
     * 
     * @param filteredMembers The filtered list to update with search predicates
     */
    public void setupSearch(FilteredList<ChatParticipant> filteredMembers) {
        if (searchField == null || filteredMembers == null) {
            logger.error("Cannot set up search: searchField={}, filteredMembers={}",
                    searchField != null ? "not null" : "null",
                    filteredMembers != null ? "not null" : "null");
            return;
        }
        
        // Ensure the initial predicate is set to show all
        filteredMembers.setPredicate(p -> true);
        logger.debug("Initial filter set to show all members");
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug("Search text changed from '{}' to '{}'", oldValue, newValue);
            
            filteredMembers.setPredicate(participant -> {
                // If search field is empty, show all members
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                // Handle null participant or user
                if (participant == null) {
                    logger.warn("Null participant encountered in search filter");
                    return false;
                }
                
                if (participant.getUser() == null) {
                    logger.warn("Participant with null user encountered in search filter");
                    return false;
                }
                
                // Convert to lowercase for case-insensitive comparison
                String lowerCaseSearch = newValue.toLowerCase();
                User user = participant.getUser();
                
                // Get user properties safely
                String username = user.getUsername();
                String email = user.getEmail();
                
                boolean matchesUsername = username != null && username.toLowerCase().contains(lowerCaseSearch);
                boolean matchesEmail = email != null && email.toLowerCase().contains(lowerCaseSearch);
                
                boolean matches = matchesUsername || matchesEmail;
                
                logger.trace("Search filter for user {}: matches={}", 
                    username != null ? username : "unknown", matches);
                    
                return matches;
            });
            
            logger.debug("Filter applied: {} of {} members visible", 
                filteredMembers.size(), filteredMembers.getSource().size());
        });
        
        logger.debug("Search functionality set up for member filtering");
    }
    
    /**
     * Loads the current user from the session.
     * 
     * @return The current user
     */
    private User loadCurrentUser() {
        try {
            String userId = this.sessionService.getElement("userId");
            if (userId == null || userId.isEmpty()) {
                logger.error("User ID not found in session");
                Platform.runLater(() -> showError("User session not found"));
                return null;
            }
            
            return this.userService.getUserById(Integer.parseInt(userId));
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format in session", e);
            Platform.runLater(() -> showError("Invalid user session"));
            return null;
        } catch (Exception e) {
            logger.error("Failed to load current user", e);
            Platform.runLater(() -> showError("Failed to load user data: " + e.getMessage()));
            return null;
        }
    }
    
    /**
     * Sets the chat for this controller and loads its members.
     * 
     * @param chat The chat to display members for
     */
    private void setChat(Chat chat) {
        logger.info("Setting chat in ChannelMembersController: {} (ID: {})", 
            chat != null ? chat.getName() : "null", 
            chat != null ? chat.getId() : "null");
            
        this.currentChat = chat;
        
        if (chat == null) {
            logger.error("Cannot set null chat in ChannelMembersController");
            showError("Cannot load chat: chat is null");
            return;
        }
        
        try {
            // Clear any previous error messages
            hideError();
            
            // Update channel name in UI
            updateChannelName(chat.getName());
            
            // Reset member list items
            members.clear();
            
            // Ensure the ListView is properly configured
            // Set ListView visibility properties
            configureListView();
            
            // Make sure the list is bound to the filtered list
            if (membersListView.getItems() != filteredMembers) {
                logger.debug("Rebinding ListView to filtered members list");
                membersListView.setItems(filteredMembers);
            }
            
            // Reset filter to show all items
            filteredMembers.setPredicate(p -> true);
            
            // Clear search field to ensure no leftover filtering
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                searchField.clear();
            }
            
            // Load members
            loadMembers();
            
            // Direct check for user role in this chat outside of the member list
            fetchCurrentUserRole();
            
        } catch (Exception e) {
            logger.error("Error initializing channel members UI", e);
            showError("Error initializing UI: " + e.getMessage());
        }
    }
    
    /**
     * Directly fetches and checks the current user's role from the database
     * This bypasses potential issues with the members list
     */
    private void fetchCurrentUserRole() {
        if (currentUser == null || currentChat == null) {
            logger.warn("Cannot fetch user role: currentUser or currentChat is null");
            canEditRoles = false;
            updatePermissionsUI(canEditRoles);
            return;
        }
        
        try {
            int userId = currentUser.getId();
            int chatId = currentChat.getId();
            
            logger.info("Directly fetching role for user ID {} in chat ID {}", userId, chatId);
            
            // Get permission status from permission service
            canEditRoles = chatPermissionService.canEditRoles(userId, chatId);
            
            // Log the determined role status
            logger.info("Direct permission check: User ID {}: canEditRoles={}", userId, canEditRoles);
            
            // Update UI based on determined permissions
            updatePermissionsUI(canEditRoles);
            updateMembersCellFactory(currentUser, canEditRoles);
            refreshMembersList();
            
        } catch (Exception e) {
            logger.error("Error fetching user role: {}", e.getMessage(), e);
            canEditRoles = false;
            updatePermissionsUI(canEditRoles);
        }
    }
    
    /**
     * Updates the channel name in the UI.
     * 
     * @param chatName The name of the current chat
     */
    private void updateChannelName(String chatName) {
        if (channelNameLabel == null) {
            logger.error("Cannot update channel name: channelNameLabel is null");
            return;
        }
        
        if (chatName == null || chatName.isEmpty()) {
            channelNameLabel.setText("Unknown Channel - Members");
            logger.warn("Setting default channel name due to null or empty input");
        } else {
            channelNameLabel.setText(chatName + " - Members");
            logger.debug("Updated channel name to: {}", chatName);
        }
    }
    
    /**
     * Configures the ListView appearance and behavior.
     * Sets appropriate size and visibility properties.
     */
    private void configureListView() {
        if (membersListView == null) {
            logger.error("Cannot configure list view: membersListView is null");
            return;
        }
        
        membersListView.setVisible(true);
        membersListView.setManaged(true);
        membersListView.setMinHeight(200);
        membersListView.setPrefHeight(300);
        
        logger.debug("ListView configured with minHeight=200, prefHeight=300");
    }
    
    /**
     * Loads members for the current chat.
     */
    private void loadMembers() {
        if (currentChat == null) {
            logger.warn("Cannot load members: current chat is null");
            return;
        }
        
        try {
            // Clear existing members
            members.clear();
            
            // Get channel participants with role information
            int chatId = currentChat.getId();
            logger.debug("Fetching participants for chat ID: {}", chatId);
            
            // Directly fetch participants from chat participant service to ensure accurate role information
            List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(chatId);
            
            if (participants == null) {
                logger.error("Participant service returned null participants list for chat {}", chatId);
                showError("Error loading members: Service returned null data");
                
                // Fallback to channel service if direct method fails
                participants = channelService.getChannelParticipants(chatId);
                if (participants == null) {
                    logger.error("Channel service also returned null participants list for chat {}", chatId);
                    return;
                }
            }
            
            if (participants.isEmpty()) {
                logger.warn("No participants found for channel {}", chatId);
            } else {
                logger.debug("Retrieved {} participants for channel {} (ID: {})", 
                    participants.size(), currentChat.getName(), chatId);
                
                // Log detailed participant information
                for (ChatParticipant p : participants) {
                    if (p != null && p.getUser() != null) {
                        logger.debug("Loaded participant: User ID: {}, Username: {}, Role: {}", 
                            p.getUser().getId(), p.getUser().getUsername(), p.getRole());
                    }
                }
            }
            
            // Ensure chat reference is set on all participants
            participants.forEach(p -> {
                if (p.getChat() == null) {
                    p.setChat(currentChat);
                }
            });
            
            // Add to observable list
            members.addAll(participants);
            logger.debug("Added {} members to observable list", members.size());
            
            // Update the list cell factory
            updateMembersCellFactory(currentUser, canEditRoles);
            
            // Force a refresh of the list
            Platform.runLater(() -> {
                refreshMembersList();
                logger.debug("ListView refreshed with {} items", membersListView.getItems().size());
                
                // Re-check permissions after loading members
                checkPermissions();
            });
        } catch (Exception e) {
            logger.error("Error loading channel members: {}", e.getMessage(), e);
            showError("Failed to load channel members: " + e.getMessage());
        }
    }
    
    /**
     * Updates the cell factory for the members list view.
     * 
     * @param currentUser The current user
     * @param canEditRoles Whether the current user can edit roles
     */
    private void updateMembersCellFactory(User currentUser, boolean canEditRoles) {
        logger.info("Updating member list cell factory with canEditRoles={}", canEditRoles);
        
        if (membersListView == null) {
            logger.error("Cannot update cell factory: membersListView is null");
            return;
        }
        
        if (currentUser == null) {
            logger.warn("Current user is null in updateMembersCellFactory");
        }
        
        try {
            membersListView.setCellFactory(listView -> {
                MemberListCell cell = new MemberListCell(this, currentUser, canEditRoles);
                logger.debug("Created new MemberListCell");
                return cell;
            });
            
            // Ensure the list view is visible and has proper size
            configureListView();
            
            // Force a refresh of the list view
            membersListView.refresh();
            
            logger.debug("Member list cell factory updated successfully");
        } catch (Exception e) {
            logger.error("Error updating member list cell factory: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Refreshes the members list view.
     */
    private void refreshMembersList() {
        if (membersListView == null) {
            logger.error("Cannot refresh members list: membersListView is null");
            return;
        }
        
        membersListView.refresh();
        logger.debug("Members list refreshed with {} items", membersListView.getItems().size());
    }
    
    /**
     * Checks if the current user has permission to edit member roles.
     */
    private void checkPermissions() {
        if (currentUser == null || currentChat == null) {
            logger.warn("Cannot check permissions: currentUser={}, currentChat={}", 
                currentUser != null ? currentUser.getId() : "null",
                currentChat != null ? currentChat.getId() : "null");
            canEditRoles = false;
            updatePermissionsUI(canEditRoles);
            return;
        }
        
        logger.info("Checking permissions for user ID: {} in chat ID: {}", currentUser.getId(), currentChat.getId());
        
        try {
            // Log current members for debugging
            logger.debug("Current members in list (before permission check):");
            for (ChatParticipant member : members) {
                if (member.getUser() != null) {
                    logger.debug("  User ID: {}, Username: {}, Role: {}", 
                        member.getUser().getId(), 
                        member.getUser().getUsername(),
                        member.getRole());
                } else {
                    logger.debug("  Member with null user reference");
                }
            }
            
            // Check if current user can edit roles using the permission service
            canEditRoles = chatPermissionService.canEditRoles(currentUser.getId(), currentChat.getId());
            logger.info("Permission check result: canEditRoles={}", canEditRoles);
            
            // Update UI elements based on permissions
            updatePermissionsUI(canEditRoles);
            
            // Update list cells to reflect permission changes
            refreshMembersList();
        } catch (Exception e) {
            logger.error("Error checking permissions: {}", e.getMessage(), e);
            canEditRoles = false;
            updatePermissionsUI(canEditRoles);
        }
    }
    
    /**
     * Updates the UI based on the user's permission to edit roles.
     * 
     * @param canEditRoles Whether the current user can edit roles
     */
    private void updatePermissionsUI(boolean canEditRoles) {
        if (permissionText == null) {
            logger.error("Cannot update permissions UI: permissionText is null");
            return;
        }
        
        // Show warning text only if user cannot edit roles
        permissionText.setVisible(!canEditRoles);
        permissionText.setManaged(!canEditRoles);
        
        logger.debug("Permission UI updated: canEditRoles={}", canEditRoles);
    }
    
    /**
     * Updates a member's role in the current chat.
     * 
     * @param participant The chat participant to update
     * @param newRole The new role for the participant
     */
    public void updateMemberRole(ChatParticipant participant, Role newRole) {
        if (participant == null || newRole == null) {
            showError("Invalid participant or role selection.");
            return;
        }
        
        User user = participant.getUser();
        if (user == null) {
            showError("Invalid user data.");
            return;
        }
        
        // Check if the current chat ID matches the participant's chat ID
        // This prevents trying to update a participant from a different chat context
        if (currentChat == null || (participant.getChat() != null && participant.getChat().getId() != currentChat.getId())) {
            logger.warn("Attempting to update participant from wrong chat context. Current chat: {}, Participant chat: {}", 
                currentChat != null ? currentChat.getId() : "null",
                participant.getChat() != null ? participant.getChat().getId() : "null");
            showError("Cannot update member: Chat context mismatch");
            return;
        }
        
        // Validate role change using permission service
        String validationError = chatPermissionService.validateRoleChange(
            currentChat.getId(),
            currentUser.getId(),
            user.getId(),
            newRole,
            participant.getRole()
        );
        
        if (validationError != null) {
            showError(validationError);
            return;
        }
        
        try {
            // Update the role in the database - Switched parameter order to match expected (userId, chatId, role)
            chatParticipantService.updateParticipantRole(
                user.getId(),  // Pass the user ID as the first parameter
                currentChat.getId(), // Pass the chat ID as the second parameter
                newRole
            );
            
            // Update the role in our local list
            participant.setRole(newRole);
            
            // Refresh the list view
            refreshMembersList();
            
            hideError();
            logger.info("Updated role for user {} to {}", user.getUsername(), newRole);
        } catch (Exception e) {
            logger.error("Error updating member role: {}", e.getMessage(), e);
            showError("Failed to update role: " + e.getMessage());
        }
    }
    
    /**
     * Displays an error message to the user.
     * 
     * @param message The error message to display
     */
    @Override
    protected void showError(String message) {
        if (errorLabel == null) {
            logger.error("Cannot show error: errorLabel is null");
            return;
        }
        
        if (message == null || message.isEmpty()) {
            logger.warn("Attempted to show empty error message");
            return;
        }
        
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        
        logger.error("UI Error: {}", message);
    }
    
    /**
     * Hides the error message.
     */
    private void hideError() {
        if (errorLabel == null) {
            logger.error("Cannot hide error: errorLabel is null");
            return;
        }
        
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        logger.debug("Error message hidden");
    }
    
    /**
     * Handles the close button action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleCloseButton(ActionEvent event) {
        this.sceneManager.closePopUp(ChannelMembersScene.class);
    }
    
    /**
     * Refreshes the members list.
     * Used by external components to update the view.
     */
    public void refreshMembers() {
        loadMembers();
        checkPermissions();
    }
    
    /**
     * Reloads all data in this controller.
     * This is the central method for refreshing the entire view.
     */
    @Override
    public void reload() {
        logger.info("Reloading ChannelMembersController");
        
        // First load the current user 
        this.currentUser = loadCurrentUser();
        
        // Then fetch the current chat ID from session
        try {
            String chatIdStr = sessionService.getElement("currentChatId");
            if (chatIdStr == null || chatIdStr.isEmpty()) {
                logger.error("No chat ID found in session during reload");
                showError("Error: No active chat selected");
                return;
            }
            
            int chatId = Integer.parseInt(chatIdStr);
            
            // Check if we're already displaying this chat
            boolean sameChat = (currentChat != null && currentChat.getId() == chatId);
            logger.debug("Reload - Current chatId={}, Session chatId={}, Same chat={}", 
                currentChat != null ? currentChat.getId() : "null", chatId, sameChat);
            
            // Always reload the chat to ensure we have the latest data
            Chat activeChat = chatService.getChatById(chatId);
            if (activeChat == null) {
                logger.error("Failed to load chat with ID: {} during reload", chatId);
                showError("Error: Could not load the selected chat");
                return;
            }
            
            // Set the new chat (this will trigger member loading)
            logger.info("Reload - Setting chat: {} (ID: {})", activeChat.getName(), activeChat.getId());
            this.currentChat = activeChat;
            
            // Clear any previous error
            hideError();
            
            // Update UI with chat name
            updateChannelName(activeChat.getName());
            
            // Clear and reload member list
            members.clear();
            
            // Load the members for this chat
            loadMembers();
            
            // Fetch and check current user role directly
            fetchCurrentUserRole();
        } catch (NumberFormatException e) {
            logger.error("Invalid chat ID format in session during reload", e);
            showError("Error: Invalid chat identifier");
        } catch (Exception e) {
            logger.error("Error during ChannelMembersController reload", e);
            showError("Error reloading view: " + e.getMessage());
        }
    }
    
    /**
     * Ensures a participant has the current chat reference properly set.
     * This helps prevent chat context mismatches when updating roles.
     * 
     * @param participant The participant that might have an incorrect or missing chat reference
     * @return The same participant with current chat reference, or null if the participant cannot be found in the current members list
     */
    public ChatParticipant getParticipantWithCurrentChat(ChatParticipant participant) {
        if (participant == null) {
            logger.warn("Cannot set chat reference: participant is null");
            return null;
        }
        
        if (participant.getUser() == null) {
            logger.warn("Cannot set chat reference: participant has null user");
            return null;
        }
        
        if (currentChat == null) {
            logger.warn("Cannot set chat reference: current chat is null");
            return null;
        }
        
        int userId = participant.getUser().getId();
        
        // Log the current state for diagnostic purposes
        logger.debug("Finding participant with user ID {} in current chat ID {}", userId, currentChat.getId());
        
        // Use permission service to find participant in list
        ChatParticipant existingParticipant = chatPermissionService.findParticipantInList(members, userId);
        
        if (existingParticipant != null) {
            // Always ensure chat reference is set to current chat
            if (existingParticipant.getChat() == null || existingParticipant.getChat().getId() != currentChat.getId()) {
                existingParticipant.setChat(currentChat);
                logger.debug("Updated chat reference for participant with user ID {} to chat ID {}", 
                    userId, currentChat.getId());
            }
            
            return existingParticipant;
        }
        
        // Participant not found in the current members list
        logger.warn("Participant with user ID {} not found in the current members list for chat ID {}", 
            userId, currentChat.getId());
            
        // Log current members for diagnostic purposes
        if (logger.isDebugEnabled()) {
            logger.debug("Current members in list:");
            for (ChatParticipant p : members) {
                if (p.getUser() != null) {
                    logger.debug("  User ID: {}, Username: {}", p.getUser().getId(), p.getUser().getUsername());
                } else {
                    logger.debug("  Participant with null user");
                }
            }
        }
        
        return null;
    }
} 