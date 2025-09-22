package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IGroupService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.GroupJoinScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.GroupService;
import com.ap4.client.services.UserService;
import com.ap4.common.models.Chat;
import com.ap4.common.models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Controller for the group join view.
 * Manages the joining of existing group chats.
 */
public class GroupJoinController extends Controller {
    private static final Logger logger = LogManager.getLogger(GroupJoinController.class);
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private ListView<String> groupListView;
    
    @FXML
    private Button joinButton;
    
    @FXML
    private Text statusText;

    private final IGroupService groupService;
    private final IUserService userService;
    private User currentUser;
    private ObservableList<Chat> availableGroups;

    /**
     * Default constructor with service initialization
     */
    public GroupJoinController() {
        this.groupService = new GroupService();
        this.userService = new UserService();
    }
    
    /**
     * Constructor with dependency injection for testing
     */
    public GroupJoinController(IGroupService groupService, IUserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Clear status message
            clearStatus();
            
            // Get current user
            this.currentUser = this.sessionService.getCurrentUser();
            if (this.currentUser == null) {
                logger.error("No current user found, cannot initialize group join view");
                showStatus("You must be logged in to join groups", true);
                disableControls(true);
                return;
            }
            
            // Initial load of available groups
            loadAvailableGroups();
        } catch (Exception e) {
            logger.error("Error initializing controller: {}", e.getMessage(), e);
            showStatus("Failed to initialize group join view", true);
            disableControls(true);
        }
    }

    /**
     * Handles the search button click event
     */
    @FXML
    private void handleSearchButton(ActionEvent event) {
        logger.debug("Search button clicked");
        searchGroups();
    }

    /**
     * Handles the join button click event
     */
    @FXML
    private void handleJoinButton(ActionEvent event) {
        logger.debug("Join button clicked");
        joinSelectedGroup();
    }

    /**
     * Loads all available groups that the user can join
     */
    private void loadAvailableGroups() {
        try {
            clearStatus();
            
            // Get all available groups using GroupService
            List<Chat> availableGroupsList = this.groupService.getAvailableGroups(this.currentUser);
            
            this.availableGroups = FXCollections.observableArrayList(availableGroupsList);
            
            updateGroupListView();
            
            if (availableGroups.isEmpty()) {
                showStatus("No available groups found", false);
            } else {
                logger.debug("Loaded {} available groups", availableGroups.size());
            }
        } catch (Exception e) {
            logger.error("Error loading available groups: {}", e.getMessage(), e);
            showStatus("Could not load available groups", true);
            disableControls(true);
        }
    }

    /**
     * Searches for groups matching the search term
     */
    private void searchGroups() {
        try {
            clearStatus();
            
            String searchTerm = this.searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                loadAvailableGroups();
                return;
            }
            
            logger.debug("Searching for groups with term: '{}'", searchTerm);
            
            // Use the service to search for groups
            List<Chat> searchResults = this.groupService.searchAvailableGroups(searchTerm, this.currentUser);
            
            this.availableGroups = FXCollections.observableArrayList(searchResults);
            updateGroupListView();
            
            if (searchResults.isEmpty()) {
                showStatus("No groups found matching: " + searchTerm, false);
            } else {
                logger.debug("Found {} groups matching search term", searchResults.size());
            }
        } catch (Exception e) {
            logger.error("Error searching groups: {}", e.getMessage(), e);
            showStatus("Search failed", true);
        }
    }

    /**
     * Joins the selected group
     */
    private void joinSelectedGroup() {
        try {
            clearStatus();
            
            String selected = groupListView.getSelectionModel().getSelectedItem();
            if (selected == null || selected.isEmpty() || "No available groups found".equals(selected)) {
                showStatus("Please select a group to join", false);
                return;
            }
            
            // Extract group name from the selection (format: "name - description")
            String groupName = selected.split(" - ")[0];
            
            // Find the group in our available groups list
            Chat groupToJoin = this.availableGroups.stream()
                .filter(chat -> chat.getName().equals(groupName))
                .findFirst()
                .orElse(null);
            
            if (groupToJoin == null) {
                logger.error("Selected group not found in available groups list: {}", groupName);
                showStatus("Group not found", true);
                return;
            }
            
            // Join the group using GroupService
            logger.debug("Attempting to join group: {}", groupToJoin.getName());
            Chat joinedGroup = this.groupService.joinGroup(groupToJoin.getId(), this.currentUser);
            
            // Success! Go back to main scene
            logger.info("Successfully joined group: {}", joinedGroup.getName());
            showStatus("Successfully joined group: " + joinedGroup.getName(), false);
            
            // Close this scene and reload the main scene
            closePopUp(GroupJoinScene.class);
            reloadScene(MainScene.class);
        } catch (IllegalArgumentException e) {
            logger.warn("Group join failed: {}", e.getMessage());
            showStatus(e.getMessage(), true);
        } catch (Exception e) {
            logger.error("Error joining group: {}", e.getMessage(), e);
            showStatus("Failed to join group", true);
        }
    }

    /**
     * Updates the group list view with available groups
     */
    private void updateGroupListView() {
        if (this.availableGroups == null || this.availableGroups.isEmpty()) {
            groupListView.setItems(FXCollections.observableArrayList(
                    "No available groups found"
            ));
            joinButton.setDisable(true);
        } else {
            // Update the ListView with group names and descriptions
            groupListView.setItems(
                    FXCollections.observableArrayList(
                            this.availableGroups.stream()
                                    .map(chat -> chat.getName() + " - " + 
                                         (chat.getDescription() != null ? chat.getDescription() : ""))
                                    .collect(Collectors.toList())
                    )
            );
            joinButton.setDisable(false);
        }
    }
    
    /**
     * Shows a status message to the user
     * 
     * @param message The message to display
     * @param isError Whether the message is an error
     */
    private void showStatus(String message, boolean isError) {
        if (statusText != null) {
            statusText.setText(isError ? "Error: " + message : message);
            statusText.setStyle(isError ? "-fx-fill: red;" : "-fx-fill: green;");
        }
    }
    
    /**
     * Clears the status message
     */
    private void clearStatus() {
        if (statusText != null) {
            statusText.setText("");
        }
    }
    
    /**
     * Enables or disables all controls
     * 
     * @param disable True to disable controls, false to enable
     */
    private void disableControls(boolean disable) {
        if (searchField != null) searchField.setDisable(disable);
        if (searchButton != null) searchButton.setDisable(disable);
        if (groupListView != null) groupListView.setDisable(disable);
        if (joinButton != null) joinButton.setDisable(disable);
    }

    @Override
    public void reload() {
        if (this.currentUser != null) {
            loadAvailableGroups();
        } else {
            // Try to get the current user again
            this.currentUser = this.sessionService.getCurrentUser();
            if (this.currentUser != null) {
                loadAvailableGroups();
                disableControls(false);
            } else {
                showStatus("You must be logged in to join groups", true);
                disableControls(true);
            }
        }
    }
}
