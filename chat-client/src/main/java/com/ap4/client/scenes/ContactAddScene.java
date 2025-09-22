package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for adding new contacts to the user's contact list.
 * Provides the user interface for searching and adding other users as contacts.
 * 
 * This scene allows users to:
 * - Search for other users by username or other identifying information
 * - View user profiles before adding them as contacts
 * - Send contact requests to users
 * - Manage pending contact requests
 * 
 * This scene loads the "ContactAdd.fxml" file which contains the UI components
 * for the contact search and add form.
 */
public class ContactAddScene extends SuperScene{
    /**
     * Creates a new ContactAddScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public ContactAddScene(Stage stage, SessionService sessionService) {
        super("ContactAdd.fxml", stage, sessionService);
    }
}
