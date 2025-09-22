package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for editing user profile information.
 * Provides the user interface for modifying user details and profile settings.
 * 
 * This scene allows users to:
 * - Update their username and password
 * - Modify personal details and contact information
 * - Adjust privacy settings and notification preferences
 * - Complete the profile editing process
 * 
 * This scene loads the "userEdit.fxml" file which contains the UI components
 * for the user editing interface.
 */
public class UserEditScene extends SuperScene{
    /**
     * Creates a new UserEditScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance containing user information
     */
    public UserEditScene(Stage stage, SessionService sessionService) {
        super("userEdit.fxml", stage, sessionService);
    }
}
