package com.ap4.client.ui.components;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.controllers.ChatListCellController;
import com.ap4.client.services.MessageService;
import com.ap4.client.services.FXMLLoaderService;
import com.ap4.client.services.FXMLLoaderService.LoadResult;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;

import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 * Custom ListCell implementation for displaying chats in a ListView.
 * Uses service architecture to access data.
 */
public class ChatListCell extends ListCell<Chat> {
    private HBox root;
    private ChatListCellController cellController;
    private final MessageService messageService;
    private static final Logger logger = LogManager.getLogger(ChatListCell.class);

    /**
     * Constructor with ChatService injection
     * 
     * @param chatService The chat service to use for data access
     */
    public ChatListCell() {
        this.messageService = new MessageService();
    }

    @Override
    protected void updateItem(Chat chat, boolean empty) {
        super.updateItem(chat, empty);

        if (empty || chat == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (root == null) {
                try {
                    LoadResult<HBox> loadResult = FXMLLoaderService.loadFXMLWithLoader("chatListCell.fxml");
                    root = loadResult.getRoot();
                    cellController = loadResult.getController();
                } catch (IOException e) {
                    logger.error("Failed to load chatListCell.fxml", e);
                    throw new RuntimeException("Failed to load chatListCell.fxml", e);
                }
            }

            cellController.getTitleLabel().setText(chat.getName());

            // Use MessageService to get the last message
            Message lastMessage = messageService.getLastMessageByChatId(chat.getId());
            if (lastMessage != null) {
                cellController.getMessageLabel().setText(lastMessage.getContent());
                cellController.getTimeLabel().setText(lastMessage.getTimestamp().toString());
            } else {
                cellController.getMessageLabel().setText("");
                cellController.getTimeLabel().setText("");
            }

            setGraphic(root);
        }
    }
    
    /**
     * Gets the controller associated with this cell
     * 
     * @return The ChatListCellController for this cell
     */
    public ChatListCellController getListCellController() {
        return this.cellController;
    }
}
