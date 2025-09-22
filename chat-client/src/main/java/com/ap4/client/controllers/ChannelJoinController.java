package com.ap4.client.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.client.scenes.ChannelJoinScene;
import com.ap4.client.scenes.MainScene;
import com.ap4.client.services.ChannelService;
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
 * Controller for joining available channels.
 * Manages the channel discovery and joining process.
 */
public class ChannelJoinController extends Controller {
    @FXML
    public TextField searchField;
    @FXML
    public Button searchButton;
    @FXML
    public ListView<String> channelListView;
    @FXML
    public Button joinButton;
    @FXML
    public Text statusText;

    private final IChannelService channelService;
    private final IUserService userService;
    private User currentUser;
    private ObservableList<Chat> availableChannels;

    /**
     * Constructor with service initialization.
     */
    public ChannelJoinController() {
        this.channelService = new ChannelService();
        this.userService = new UserService();
    }

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        try {
            int userId = Integer.parseInt(this.sessionService.getElement("userId"));
            this.currentUser = this.userService.getUserById(userId);
            
            // Initial load of available channels
            loadAvailableChannels();
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            this.statusText.setText("Error: Could not get current user information");
        }
    }

    /**
     * Handles the search button click.
     */
    @FXML
    private void handleSearchButton(ActionEvent event) {
        searchChannels();
    }

    /**
     * Handles the join button click.
     */
    @FXML
    private void handleJoinButton(ActionEvent event) {
        joinSelectedChannel();
    }

    /**
     * Loads all available channels the user can join.
     */
    private void loadAvailableChannels() {
        try {
            // Get available channels from the channel service
            List<Chat> availableChannelsList = this.channelService.getAvailableChannels(currentUser);
            
            this.availableChannels = FXCollections.observableArrayList(availableChannelsList);
            
            updateChannelListView();
        } catch (Exception e) {
            logger.error("Error loading available channels: {}", e.getMessage());
            this.statusText.setText("Error: Could not load available channels");
        }
    }

    /**
     * Searches for channels based on the search term.
     */
    private void searchChannels() {
        String searchTerm = this.searchField.getText().trim();
        
        try {
            // Use the channel service method to search for channels
            List<Chat> searchResults = this.channelService.searchChannels(searchTerm, currentUser);
            
            // Update the available channels and the list view
            this.availableChannels = FXCollections.observableArrayList(searchResults);
            updateChannelListView();
        } catch (Exception e) {
            logger.error("Error searching channels: {}", e.getMessage());
            this.statusText.setText("Error: Could not search channels");
        }
    }

    /**
     * Joins the selected channel.
     */
    private void joinSelectedChannel() {
        String selected = channelListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            this.statusText.setText("Please select a channel to join");
            return;
        }
        
        try {
            // Extract channel name from the selection (format: "name - description")
            String channelName = selected.split(" - ")[0];
            
            // Use channel service to find the channel by name
            Chat channelToJoin = channelService.findChannelByName(channelName, this.availableChannels);
                
            if (channelToJoin == null) {
                this.statusText.setText("Error: Channel not found");
                return;
            }
            
            // Use channel service to join the channel
            channelService.joinChannel(channelToJoin.getId(), currentUser);
            
            // Success! Go back to main scene
            this.statusText.setText("Successfully joined channel: " + channelName);
            this.sceneManager.reloadScene(MainScene.class);
            this.close(ChannelJoinScene.class);
        } catch (IllegalArgumentException e) {
            this.statusText.setText(e.getMessage());
        } catch (Exception e) {
            logger.error("Error joining channel: {}", e.getMessage());
            this.statusText.setText("Error: Could not join channel");
        }
    }

    /**
     * Updates the channel list view with the current available channels.
     */
    private void updateChannelListView() {
        // Update the ListView with channel names and descriptions
        channelListView.setItems(
                FXCollections.observableArrayList(
                        this.availableChannels.stream()
                                .map(chat -> chat.getName() + " - " + 
                                     (chat.getDescription() != null ? chat.getDescription() : ""))
                                .collect(Collectors.toList())
                )
        );
    }

    /**
     * Reloads the controller by refreshing the available channels.
     */
    @Override
    public void reload() {
        loadAvailableChannels();
    }
}
