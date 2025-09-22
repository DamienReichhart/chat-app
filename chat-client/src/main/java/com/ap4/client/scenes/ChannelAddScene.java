package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for creating new channels.
 * Provides the user interface for defining new channel properties and settings.
 * 
 * This scene allows users to:
 * - Create a new public or private channel
 * - Set a name and optional description for the channel
 * - Define channel visibility and membership permissions
 * - Set channel topic and initial metadata
 * 
 * This scene loads the "channelAdd.fxml" file which contains the UI components
 * for the channel creation form.
 */
public class ChannelAddScene extends SuperScene{
    /**
     * Creates a new ChannelAddScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public ChannelAddScene(Stage stage, SessionService sessionService) {
        super("channelAdd.fxml", stage, sessionService);
    }
}
