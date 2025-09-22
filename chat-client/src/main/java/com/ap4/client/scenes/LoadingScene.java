package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for displaying a loading indicator during application initialization.
 * Provides a visual indicator while user data is loaded and cached in the background.
 * 
 * This scene is displayed:
 * - After successful login authentication
 * - Before transition to the main application interface
 * - During initial data retrieval, including chats, contacts, and recent messages
 * - While caching operations are preparing the application for use
 * 
 * This scene loads the "loading.fxml" file which contains the UI components
 * for the loading animation and progress indicators.
 */
public class LoadingScene extends SuperScene {
    /**
     * Creates a new LoadingScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance containing user information
     */
    public LoadingScene(Stage stage, SessionService sessionService) {
        super("loading.fxml", stage, sessionService);
    }

}
