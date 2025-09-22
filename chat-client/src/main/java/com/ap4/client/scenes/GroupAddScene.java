package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for creating new chat groups.
 * Provides the user interface for defining new group properties and adding members.
 * 
 * This scene allows users to:
 * - Set a name for the new group
 * - Add an optional description
 * - Select initial members from their contacts
 * - Configure group visibility and permissions
 * 
 * This scene loads the "groupAdd.fxml" file which contains the UI components
 * for the group creation form.
 */
public class GroupAddScene extends SuperScene{
    /**
     * Creates a new GroupAddScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public GroupAddScene(Stage stage, SessionService sessionService) {
        super("groupAdd.fxml", stage, sessionService);
    }
}
