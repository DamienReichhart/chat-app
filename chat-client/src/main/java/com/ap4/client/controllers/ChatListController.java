package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.services.ChatParticipantService;
import com.ap4.client.services.ChatService;
import com.ap4.client.ui.components.ChatListCell;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;
import com.ap4.client.exceptions.data.ChatNotFoundException;
import com.ap4.client.exceptions.data.ChatParticipantNotFoundException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.ui.UIUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller class for managing the list of chats displayed in the UI.
 * Handles chat list initialization, filtering, and selection.
 */
public class ChatListController extends Controller {
    private static final Logger logger = LogManager.getLogger(ChatListController.class);
    
    @FXML
    private ListView<Chat> contactListView;
    @FXML
    private TextField searchField;

    private ObservableList<Chat> originalChatList;
    private FilteredList<Chat> filteredChatList;
    
    // Service instances
    private final IChatService chatService;
    private final IChatParticipantService chatParticipantService;
    
    /**
     * Default constructor with service initialization
     */
    public ChatListController() {
        super();
        this.chatService = new ChatService();
        this.chatParticipantService = new ChatParticipantService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public ChatListController(IChatService chatService, IChatParticipantService chatParticipantService) {
        super();
        this.chatService = chatService;
        this.chatParticipantService = chatParticipantService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // We're passing the chatService to the cell factory so it can access chat data
        contactListView.setCellFactory(listView -> new ChatListCell());
        
        // Initialize collections
        originalChatList = FXCollections.observableArrayList();
        filteredChatList = new FilteredList<>(originalChatList);
        contactListView.setItems(filteredChatList);
        
        // Set up search functionality
        setupSearch();
        
        // Initialize the chat list
        this.initChatList();
    }

    /**
     * Sets up the search functionality for filtering chats
     */
    private void setupSearch() {
        // Add listener to searchField for instant filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredChatList.setPredicate(chat -> {
                // If search field is empty, show all chats
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against chat name
                if (chat.getName() != null && chat.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Match against chat description
                if (chat.getDescription() != null && chat.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // No match found
                return false;
            });
        });
    }

    /**
     * Initializes the chat list with chats the current user has access to
     */
    private void initChatList() {
        try {
            // Clear lists before adding new items
            originalChatList.clear();
            
            // Get current user
            User currentUser = sessionService.getCurrentUser();
            if (currentUser == null) {
                logger.error("Current user is null, cannot initialize chat list");
                return;
            }
            
            try {
                // Get all chats the user has access to using the ChatParticipantService
                List<ChatParticipant> chatsAccess = chatParticipantService.getParticipantsByUserId(currentUser.getId());
                
                // Add each chat to the list
                for (ChatParticipant chatParticipant : chatsAccess) {
                    try {
                        Chat chat = chatService.getChatById(chatParticipant.getChat().getId());
                        // The ChatListCell will use services to get any additional information when needed
                        originalChatList.add(chat);
                    } catch (ChatNotFoundException e) {
                        // Log but continue with other chats if one is not found
                        logger.warn("Chat not found during list initialization: {}", e.getMessage());
                    } catch (DataAccessException e) {
                        // Log database access issues for specific chats but continue with others
                        logger.warn("Database error while accessing chat: {}", e.getMessage());
                    }
                }
                
                logger.debug("Initialized chat list with {} chats", originalChatList.size());
            } catch (ChatParticipantNotFoundException e) {
                logger.warn("No chat participants found for user: {}", e.getMessage());
                // This is normal for new users, just show empty list
            } catch (DataAccessException e) {
                logger.error("Database error while fetching chat participants: {}", e.getMessage(), e);
                UIUtils.showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not retrieve your chats. Please try again later.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error initializing chat list: {}", e.getMessage(), e);
            UIUtils.showAlert(Alert.AlertType.ERROR, "Application Error", 
                "An unexpected error occurred. Please restart the application.");
        }
    }

    @Override
    public void reload() {
        try {
            originalChatList.clear();
            this.initChatList();
        } catch (DataAccessException e) {
            logger.error("Database error reloading chat list: {}", e.getMessage(), e);
            UIUtils.showAlert(Alert.AlertType.ERROR, "Database Error", 
                "Could not reload your chats. Please try again later.");
        } catch (Exception e) {
            logger.error("Error reloading chat list: {}", e.getMessage(), e);
            UIUtils.showAlert(Alert.AlertType.ERROR, "Application Error", 
                "An unexpected error occurred while refreshing your chats.");
        }
    }

    /**
     * Gets the contact list view component
     * 
     * @return The ListView containing chats
     */
    public ListView<Chat> getContactListView() {
        return contactListView;
    }
    
    /**
     * Clears the search field - useful for resetting the search
     */
    public void clearSearch() {
        searchField.clear();
    }
}
