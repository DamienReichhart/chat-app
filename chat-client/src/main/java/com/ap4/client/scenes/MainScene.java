package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Main application scene that serves as the primary user interface.
 * This scene is displayed after successful login and contains the core
 * chat functionality including chat list, message area, and user controls.
 * 
 * The scene uses a wider width (1000px) than the default to accommodate
 * the multi-panel layout of the main interface.
 * 
 * This scene loads the "main.fxml" file which contains the UI components
 * for the chat application's main interface.
 */
public class MainScene extends SuperScene{
    /**
     * Creates a new MainScene instance.
     * Sets a custom width to accommodate the chat interface layout.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public MainScene(Stage stage, SessionService sessionService) {
        super("main.fxml", stage, sessionService);
        // Set a wider scene to accommodate multiple panels
        this.setSceneWidth(1300);
    }
}
