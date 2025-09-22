package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for editing existing chat group properties.
 * Provides the user interface for modifying group information and membership.
 * 
 * This scene allows group administrators to:
 * - Update the group's name and description
 * - Add or remove group members
 * - Modify group permissions and visibility settings
 * - Transfer group ownership if needed
 * 
 * This scene loads the "groupEdit.fxml" file which contains the UI components
 * for the group editing interface.
 */
public class GroupEditScene extends SuperScene {
    /**
     * Creates a new GroupEditScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance containing the group data
     */
    public GroupEditScene(Stage stage, SessionService sessionService) {
        super("groupEdit.fxml", stage, sessionService);
    }
}
