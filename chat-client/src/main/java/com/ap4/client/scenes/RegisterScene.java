package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for user registration.
 * Provides the user interface for creating a new account in the chat application.
 * 
 * This scene allows new users to:
 * - Create a username and password
 * - Enter personal details and profile information
 * - Accept terms of service and privacy policy
 * - Complete the registration process
 * 
 * This scene has a taller height (650px) than the default to accommodate
 * the registration form fields and validation messages.
 * 
 * This scene loads the "register.fxml" file which contains the UI components
 * for the registration form.
 */
public class RegisterScene extends SuperScene{
    /**
     * Creates a new RegisterScene instance.
     * Sets a custom height to accommodate the registration form.
     * 
     * @param primaryStage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public RegisterScene(Stage primaryStage, SessionService sessionService) {
        super("register.fxml" ,primaryStage, sessionService);
        // Set a taller scene to accommodate registration form fields
        this.setSceneHeight(650);
    }

}
