package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for editing existing channel properties.
 * Provides the user interface for modifying channel information and settings.
 * 
 * This scene allows channel administrators to:
 * - Update the channel's name and description
 * - Modify channel visibility and membership permissions
 * - Manage channel member roles and access levels
 * - Configure channel-specific preferences
 * 
 * This scene loads the "channelEdit.fxml" file which contains the UI components
 * for the channel editing interface.
 */
public class ChannelEditScene extends SuperScene {
    /**
     * Creates a new ChannelEditScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance containing the channel data
     */
    public ChannelEditScene(Stage stage, SessionService sessionService) {
        super("channelEdit.fxml", stage, sessionService);
    }
}
