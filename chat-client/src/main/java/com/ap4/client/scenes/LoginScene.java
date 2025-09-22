package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for user authentication.
 * Provides the user interface for logging into the chat application.
 * 
 * This scene is typically the first scene displayed to the user when
 * they start the application and are not already authenticated. It
 * contains form fields for entering credentials and buttons for
 * submitting those credentials or navigating to the registration screen.
 * 
 * This scene loads the "login.fxml" file which contains the UI components
 * for user authentication.
 */
public class LoginScene extends SuperScene{
    /**
     * Creates a new LoginScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public LoginScene(Stage stage, SessionService sessionService) {
        super("login.fxml", stage, sessionService);
        setSceneHeight(600);
        setSceneWidth(300);
    }
}
