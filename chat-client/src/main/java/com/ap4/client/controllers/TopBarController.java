package com.ap4.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IAuthenticationService;
import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IGroupService;
import com.ap4.client.scenes.ChannelAddScene;
import com.ap4.client.scenes.ChannelJoinScene;
import com.ap4.client.scenes.ChannelMembersScene;
import com.ap4.client.scenes.ContactAddScene;
import com.ap4.client.scenes.GroupAddScene;
import com.ap4.client.scenes.GroupJoinScene;
import com.ap4.client.scenes.LoginScene;
import com.ap4.client.scenes.SceneManager;
import com.ap4.client.scenes.SuperScene;
import com.ap4.client.scenes.UserEditScene;
import com.ap4.client.services.AuthenticationService;
import com.ap4.client.services.ChannelService;
import com.ap4.client.services.GroupService;
import com.ap4.client.services.WebSocketService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.models.Chat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

/**
 * Controller for the top navigation bar of the application.
 * Manages the application's main menu, search functionality, title display,
 * and provides access to various features like creating/joining channels and groups.
 * This controller communicates with the ChatContentController to control search functionality.
 */
public class TopBarController extends Controller {
    private static final Logger logger = LogManager.getLogger(TopBarController.class);
    
    /**
     * Button that opens the options dropdown menu.
     */
    @FXML
    public Button menuOptionsButton; // Changed from optionsButton to match our FXML
    
    /**
     * Button that toggles the search functionality.
     */
    @FXML
    public Button searchButton;      // New button for search
    
    /**
     * Button for initiating calls (functionality placeholder).
     */
    @FXML
    public Button callButton;        // New button for calls
    
    /**
     * Button for logging out of the application.
     */
    @FXML
    public Button logoutButton;      // New button for logout
    
    /**
     * Label displaying the title of the current view (usually chat name).
     */
    @FXML
    public Label titleLabel;
    
    /**
     * Label displaying the online status information.
     */
    @FXML
    public Label statusLabel;        // New label for online status
    
    /**
     * Label displaying the avatar text (usually first letter of name).
     */
    @FXML
    public Label avatarLabel;        // New label for avatar text

    /**
     * Context menu for the options button.
     */
    private ContextMenu contextMenu;
    
    /**
     * Flag to track whether the search bar is visible.
     */
    private boolean isSearchVisible = false;
    
    /**
     * Reference to the chat content controller for communication.
     */
    private ChatContentController chatContentController;

    /**
     * Menu items for the context menu.
     */
    private MenuItem changeProfileOption;
    private MenuItem createNewChannel;
    private MenuItem joinExistingChannel;
    private MenuItem joinExistingGroup;
    private MenuItem addContactOption;
    private MenuItem addGroupOption;

    private Chat currentChat;
    private IChannelService channelService;
    private IGroupService groupService;
    private IAuthenticationService authService;
    private WebSocketService webSocketService;

    /**
     * Default constructor
     */
    public TopBarController() {
        super();
        this.channelService = new ChannelService();
        this.groupService = new GroupService();
        this.authService = new AuthenticationService();
        this.webSocketService = WebSocketService.getInstance();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public TopBarController(IChannelService channelService, IGroupService groupService, 
                          IAuthenticationService authService, WebSocketService webSocketService) {
        super();
        this.channelService = channelService;
        this.groupService = groupService;
        this.authService = authService;
        this.webSocketService = webSocketService;
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
        // Check if sceneManager is initialized, if not, get it from the singleton
        if (this.sceneManager == null) {
            this.sceneManager = SceneManager.getInstance();
            logger.debug("SceneManager initialized from singleton in TopBarController");
        }
        
        // Initialize the dropdown menu and its options
        initMenu();

        // Setup click handler for title to show members
        setupTitleClickHandler();
    }
    
    /**
     * Sets the ChatContentController reference to enable communication between controllers.
     * This allows the top bar to control features in the chat content area,
     * such as toggling the search bar visibility.
     * 
     * @param controller The ChatContentController to communicate with
     */
    public void setChatContentController(ChatContentController controller) {
        this.chatContentController = controller;
    }
    
    /**
     * Handles search button action event
     *
     * @param event The ActionEvent triggered by clicking the search button
     */
    @FXML
    private void handleSearchButton(ActionEvent event) {
        logger.info("Search button clicked");
        toggleSearchBar();
    }
    
    /**
     * Handles call button action event
     *
     * @param event The ActionEvent triggered by clicking the call button
     */
    @FXML
    private void handleCallButton(ActionEvent event) {
        logger.info("Call button clicked");
        // Add call functionality here
    }
    
    /**
     * Handles menu options button action event
     *
     * @param event The ActionEvent triggered by clicking the menu options button
     */
    @FXML
    private void handleMenuOptionsButton(ActionEvent event) {
        if (contextMenu != null) {
            contextMenu.show(menuOptionsButton, 
                        menuOptionsButton.localToScreen(0, menuOptionsButton.getHeight()).getX(), 
                        menuOptionsButton.localToScreen(0, menuOptionsButton.getHeight()).getY());
        }
    }
    
    /**
     * Handles logout button action event.
     * Resets the session, closes the main window, and opens the login window.
     *
     * @param event The ActionEvent triggered by clicking the logout button
     */
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        logger.info("Logout button clicked");
        
        try {
            // Reset WebSocket connection
            webSocketService.reset();
            
            // Reset current chat and controllers
            if (chatContentController != null) {
                chatContentController.cleanup();
            }
            
            // Perform logout using the authentication service
            authService.logout();
            
            // Clear any cached data
            this.currentChat = null;
            
            // Switch to the login scene
            switchScene(LoginScene.class);
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            showError("Error during logout");
        }
    }
    
    /**
     * Toggles the search bar visibility with animation in the ChatContentController.
     * This method updates the isSearchVisible flag and communicates with the
     * ChatContentController to show or hide the search interface.
     */
    private void toggleSearchBar() {
        // Toggle the visibility state
        isSearchVisible = !isSearchVisible;
        
        // Communicate with ChatContentController to actually show/hide the search bar
        if (chatContentController != null) {
            chatContentController.toggleSearchBarVisibility(isSearchVisible);
        } else {
            logger.error("ChatContentController is null - can't toggle search bar");
        }
    }

    /**
     * Initializes the menu system by creating options, the context menu,
     * registering actions, and attaching the menu to its button.
     * This method organizes the menu initialization process into separate steps.
     */
    private void initMenu() {
        // Create menu option objects
        createMenuOptions();
        
        // Build the context menu with the options
        createContextMenu();
        
        // Register action handlers for menu items
        registerMenuOptionsActions();
        
        // Attach the context menu to the button
        if (menuOptionsButton != null) {
            registerContextMenu();
        } else {
            logger.error("Menu options button is null - can't register context menu");
        }
    }

    /**
     * Creates the individual menu option items.
     * Initializes all the MenuItem objects that will be added to the context menu.
     */
    private void createMenuOptions() {
        this.createNewChannel = new MenuItem("Créer un canal");
        this.changeProfileOption = new MenuItem("Changer son profil");
        this.joinExistingChannel = new MenuItem("Rejoindre un canal");
        this.joinExistingGroup = new MenuItem("Rejoindre un groupe");
        this.addContactOption = new MenuItem("Ajouter un contact");
        this.addGroupOption = new MenuItem("Ajouter un groupe");
    }

    /**
     * Creates the context menu and adds all menu options to it.
     * This populates the dropdown menu that appears when clicking the options button.
     */
    private void createContextMenu() {
        this.contextMenu = new ContextMenu();
        this.contextMenu.getItems().addAll(
            this.changeProfileOption, 
            this.createNewChannel,
            this.joinExistingChannel,
            this.joinExistingGroup,
            this.addContactOption,
            this.addGroupOption
        );
    }

    /**
     * Registers the context menu to appear when the options button is clicked.
     * Sets up the event handler to display the menu at the appropriate position.
     */
    private void registerContextMenu() {
        // With FXML onAction now handling the click event, this method is simplified
        // The context menu is displayed directly in the handleMenuOptionsButton method
    }

    /**
     * Registers action handlers for all menu items.
     * Each menu option is associated with a specific action, typically opening
     * a popup scene or performing a specific operation.
     */
    private void registerMenuOptionsActions() {
        // Set up handlers for each menu option
        if (this.changeProfileOption != null) {
            this.changeProfileOption.setOnAction(event -> {
                logger.trace("Opening UserEditScene");
                safeOpenPopUpScene(UserEditScene.class);
            });
        }

        if (this.createNewChannel != null) {
            this.createNewChannel.setOnAction(event -> {
                logger.trace("Opening ChannelAddScene");
                safeOpenPopUpScene(ChannelAddScene.class);
            });
        }

        if (this.joinExistingChannel != null) {
            this.joinExistingChannel.setOnAction(event -> {
                logger.trace("Opening ChannelJoinScene");
                safeOpenPopUpScene(ChannelJoinScene.class);
            });
        }

        if (this.joinExistingGroup != null) {
            this.joinExistingGroup.setOnAction(event -> {
                logger.trace("Opening GroupJoinScene");
                safeOpenPopUpScene(GroupJoinScene.class);
            });
        }

        if (this.addContactOption != null) {
            this.addContactOption.setOnAction(event -> {
                logger.trace("Opening ContactAddScene");
                safeOpenPopUpScene(ContactAddScene.class);
            });
        }

        if (this.addGroupOption != null) {
            this.addGroupOption.setOnAction(event -> {
                logger.trace("Opening GroupAddScene");
                safeOpenPopUpScene(GroupAddScene.class);
            });
        }
    }

    /**
     * Sets up a click handler for the title label to open the channel members UI.
     * This allows users to view and manage members by clicking on the channel/group name.
     */
    private void setupTitleClickHandler() {
        if (titleLabel != null) {
            titleLabel.setOnMouseClicked(this::handleTitleClick);
            // Add visual cue that this is clickable
            titleLabel.getStyleClass().add("clickable-title");
        }
    }

    /**
     * Handles clicks on the title label.
     * Opens the channel/group members management UI when the title is clicked.
     *
     * @param event The MouseEvent for the click
     */
    private void handleTitleClick(MouseEvent event) {
        // Only proceed if we have a current chat
        if (currentChat == null) {
            return;
        }

        // Only show members for channels and groups, not individual chats
        if (currentChat.getChatType() == ChatType.INDIVIDUAL) {
            return;
        }
        
        // Store chat ID and name in session
        sessionService.setElement("currentChatId", String.valueOf(currentChat.getId()));
        sessionService.setElement("currentChatName", currentChat.getName());

        // Determine which scene to open based on chat type
        if (currentChat.getChatType() == ChatType.CHANEL) {
            // For channels, ensure it's actually a valid channel
            Chat channel = channelService.getChannelById(currentChat.getId());
            if (channel == null) {
                showError("This chat is not a valid channel");
                logger.warn("Cannot open channel members: chat {} is not a valid channel", currentChat.getId());
                return;
            }
            
            logger.info("Opening members management for: {}", currentChat.getName());
            safeOpenPopUpScene(ChannelMembersScene.class);
        } else if (currentChat.getChatType() == ChatType.GROUP) {
            // For groups, could implement group members management here
            // or show an appropriate message
            showError("You cannot modify members roles in a group");
        } else {
            // Failsafe for unexpected chat types
            logger.warn("Unknown chat type for members management: {}", currentChat.getChatType());
            showError("Cannot manage members for this chat type");
        }
    }

    /**
     * Sets the title text in the top bar and stores the current chat.
     * Updates the title label with the provided text, typically the name of the current chat.
     * 
     * @param title The title to display
     * @param chat The current chat object
     */
    public void setTitle(String title, Chat chat) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        this.currentChat = chat;
    }
    
    /**
     * Sets the status text in the top bar.
     * Updates the status label with the provided text, typically online/offline status.
     * 
     * @param status The status to display
     */
    public void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }
    
    /**
     * Sets the avatar text, typically the first letter of a name.
     * This is used for visual identification in the UI when no image is available.
     * 
     * @param name The name to extract first letter from
     */
    public void setAvatar(String name) {
        if (avatarLabel != null && name != null && !name.isEmpty()) {
            avatarLabel.setText(name.substring(0, 1).toUpperCase());
        }
    }

    /**
     * Reloads the controller's data and view elements.
     * Currently a placeholder for future functionality if needed.
     */
    @Override
    public void reload() {
        // No implementation needed for now
    }

    /**
     * Ensures the sceneManager is initialized before using it.
     * This is a safety method to prevent NullPointerException.
     */
    private void ensureSceneManagerInitialized() {
        if (this.sceneManager == null) {
            this.sceneManager = SceneManager.getInstance();
            logger.debug("SceneManager initialized from singleton before use in TopBarController");
        }
    }

    /**
     * Opens a popup scene with a check to ensure sceneManager is initialized first.
     * 
     * @param sceneClass The scene class to open
     */
    private void safeOpenPopUpScene(Class<? extends SuperScene> sceneClass) {
        ensureSceneManagerInitialized();
        if (this.sceneManager != null) {
            logger.debug("Opening popup scene safely: {}", sceneClass.getSimpleName());
            this.openPopUpScene(sceneClass);
        } else {
            logger.error("Cannot open popup scene: SceneManager is null");
            // Show error to user
            showError("System error: Cannot open window");
        }
    }
}
