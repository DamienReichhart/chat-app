package com.ap4.client.scenes;

import com.ap4.client.services.SessionService;
import javafx.stage.Stage;

/**
 * Scene implementation for sending and displaying chat messages.
 * Provides the user interface for real-time messaging and file sharing.
 * 
 * Key features include:
 * - Text message display and sending
 * - File sharing capabilities supporting various file types
 *   - Image files (jpg, jpeg, png, gif) that display directly in the chat UI
 *   - Video files (mp4, avi, mov, wmv) that can be played using the system's default player
 *   - Other file types that can be downloaded by the recipient
 * - Message history viewing
 * 
 * This scene loads the "chatContent.fxml" file which contains the UI components
 * for the messaging interface, including text input, message display area,
 * and file attachment controls.
 */
public class MessageScene extends SuperScene {
    
    /**
     * Creates a new MessageScene instance.
     * 
     * @param stage The JavaFX stage to display this scene on
     * @param sessionService The application session instance
     */
    public MessageScene(Stage stage, SessionService sessionService) {
        super("chatContent.fxml", stage, sessionService);
    }
    
    /**
     * Reloads the scene content.
     * Ensures that the message display is refreshed with the latest messages
     * and file attachments.
     */
    @Override
    public void reload() {
        super.reload();
    }
}
