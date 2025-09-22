package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for joining existing channels.
 * Provides the user interface for browsing and joining available public channels.
 * 
 * This scene loads the "channelJoin.fxml" file which contains the UI components
 * for displaying available channels and handling the join process.
 */
public class ChannelJoinScene extends SuperScene {
    /**
     * Creates a new ChannelJoinScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public ChannelJoinScene(Stage stage, SessionService sessionService) {
        super("channelJoin.fxml", stage, sessionService);
        this.setSceneHeight(600);
    }
}
