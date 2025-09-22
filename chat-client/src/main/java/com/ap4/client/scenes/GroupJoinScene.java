package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for joining existing groups.
 * Provides the user interface for browsing and joining available groups.
 * 
 * This scene allows users to:
 * - View a list of available groups they can join
 * - See group details including name, description, and member count
 * - Filter groups by visibility or membership criteria
 * - Submit requests to join groups that require approval
 * - Join public groups directly
 * 
 * This scene loads the "groupJoin.fxml" file which contains the UI components
 * for the group browsing and joining interface.
 */
public class GroupJoinScene extends SuperScene {
    /**
     * Creates a new GroupJoinScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public GroupJoinScene(Stage stage, SessionService sessionService) {
        super("groupJoin.fxml", stage, sessionService);
        this.setSceneHeight(600);
    }
}
