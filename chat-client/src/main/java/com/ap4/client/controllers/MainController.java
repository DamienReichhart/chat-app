package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.services.WebSocketService;
import com.ap4.common.models.Chat;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the main application view.
 * This controller coordinates and manages the interaction between various components of the main view,
 * including the chat list, chat content area, and top navigation bar.
 * It serves as a central hub that facilitates communication between sub-controllers.
 */
public class MainController extends Controller {
    // Logger for this class
    private static final Logger logger = LogManager.getLogger(MainController.class);
    
    /**
     * The main border pane that contains all UI components of the main view.
     */
    @FXML
    public BorderPane mainBorderPane;
    
    /**
     * The top bar container that houses navigation and action buttons.
     */
    @FXML
    public HBox topBar;
    
    /**
     * The content area where chat messages are displayed.
     */
    @FXML
    public BorderPane chatContent;
    
    /**
     * The container for the chat list sidebar.
     */
    @FXML
    public VBox chatList;
    
    /**
     * Controller for the chat list component.
     * Injected by FXML loader.
     */
    @FXML
    private ChatListController chatListController;
    
    /**
     * Controller for the top bar component.
     * Injected by FXML loader.
     */
    @FXML
    private TopBarController topBarController;
    
    /**
     * Controller for the chat content area.
     * Injected by FXML loader.
     */
    @FXML
    private ChatContentController chatContentController;

    private final WebSocketService webSocketService;
    
    /**
     * Default constructor.
     */
    public MainController() {
        this.webSocketService = WebSocketService.getInstance();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param webSocketService The websocket service implementation
     */
    public MainController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    /**
     * Initializes the controller with required resources and sets up component interactions.
     * This method is automatically called after the FXML has been loaded.
     *
     * @param location  The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Connect sub-controllers to enable inter-controller communication
            setupControllerCommunication();
            
            // Get the list view from the chat list controller
            ListView<Chat> listView = chatListController.getContactListView();
    
            // Add listener for chat selection changes
            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
                if (newChat != null) {
                    logger.debug("User selected chat: {}", newChat.getName());
                    loadChatContent(newChat);
                }
            });
        } catch (Exception e) {
            logger.error("Error initializing MainController: {}", e.getMessage(), e);
            showError("Failed to initialize main view");
        }
    }
    
    /**
     * Sets up communication channels between sub-controllers.
     * This allows controllers to interact with each other in a structured way.
     */
    private void setupControllerCommunication() {
        // Connect TopBarController with ChatContentController
        if (topBarController != null && chatContentController != null) {
            topBarController.setChatContentController(chatContentController);
        } else {
            logger.error("Cannot connect controllers: topBarController or chatContentController is null");
        }
    }

    /**
     * Reloads all components of the main view.
     * This includes refreshing the chat list and the currently selected chat content.
     * This method is called when the main view needs to be updated, such as after
     * a new message is received or a chat is added/removed.
     */
    @Override
    public void reload() {
        try {
            // Reload the chat list
            if (chatListController != null) {
                chatListController.reload();
            }
    
            // Reload the chat content
            if (chatContentController != null) {
                chatContentController.reload();
            }
            
            // Get currently selected chat and reload it if any
            ListView<Chat> listView = chatListController.getContactListView();
            Chat selectedChat = listView.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                loadChatContent(selectedChat);
            }
        } catch (Exception e) {
            logger.error("Error reloading MainController: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads chat content for the selected chat.
     * Updates both the chat content area and the top bar with the selected chat's information.
     * 
     * @param chat The selected chat to display
     */
    private void loadChatContent(Chat chat) {
        if (chat == null || chatContentController == null) {
            return;
        }
        
        try {
            // Set the current chat in the content controller and reload messages
            chatContentController.setChat(chat);
            
            // Update top bar with chat information if needed
            if (topBarController != null) {
                topBarController.setTitle(chat.getName(), chat);
            }
        } catch (Exception e) {
            logger.error("Error loading chat content for chat {}: {}", 
                        chat.getName(), e.getMessage(), e);
            showError("Failed to load chat content");
        }
    }
    
    /**
     * Returns the ChatContentController for external access.
     * This allows other components to interact with the chat content area.
     * 
     * @return The ChatContentController instance
     */
    public ChatContentController getChatContentController() {
        return chatContentController;
    }
    
    /**
     * Returns the TopBarController for external access.
     * This allows other components to interact with the top navigation bar.
     * 
     * @return The TopBarController instance
     */
    public TopBarController getTopBarController() {
        return topBarController;
    }

    /**
     * Cleans up resources when the controller is no longer needed.
     * Should be called before the application exits.
     */
    public void cleanup() {
        logger.info("Cleaning up MainController resources");
        
        try {
            // Clean up chat content controller
            if (chatContentController != null) {
                chatContentController.cleanup();
            }
            
            // Disconnect from WebSocket
            if (webSocketService != null) {
                webSocketService.disconnect();
            }
        } catch (Exception e) {
            logger.error("Error during MainController cleanup: {}", e.getMessage(), e);
        }
    }
}
