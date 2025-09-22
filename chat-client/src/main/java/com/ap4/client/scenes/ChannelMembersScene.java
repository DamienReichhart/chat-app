package com.ap4.client.scenes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.services.ChannelService;
import com.ap4.client.services.SessionService;

import javafx.stage.Stage;

/**
 * Scene implementation for managing channel/group members.
 * Provides the user interface for viewing members and changing their roles.
 * 
 * This scene allows administrators and owners to:
 * - View all members of a channel or group
 * - Change member roles (upgrade to admin or downgrade to member)
 * - See the current status of each member
 * 
 * This scene loads the "channelMembers.fxml" file which contains the UI components
 * for the member management interface.
 */
public class ChannelMembersScene extends SuperScene {
    private static final Logger logger = LogManager.getLogger(ChannelMembersScene.class);
    private final IChannelService channelService;
    
    /**
     * Creates a new ChannelMembersScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance containing the channel data
     */
    public ChannelMembersScene(Stage stage, SessionService sessionService) {
        super("channelMembers.fxml", stage, sessionService);
        this.channelService = new ChannelService();
        
        // Set a proper size for this dialog
        setSceneWidth(600);
        setSceneHeight(500);
    }
} 