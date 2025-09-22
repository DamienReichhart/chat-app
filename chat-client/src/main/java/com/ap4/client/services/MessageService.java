package com.ap4.client.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ap4.client.db.dao.MessageDAO;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.MessageNotFoundException;
import com.ap4.client.exceptions.websocket.WebSocketMessageException;
import com.ap4.client.interfaces.db.dao.IMessageDAO;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.enums.ContentType;
import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Service class for message-related operations.
 * Handles creation, retrieval, and management of messages.
 */
public class MessageService implements IMessageService {
    private final IMessageDAO messageDAO;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private WebSocketService webSocketService;

    /**
     * Constructor with dependency injection for the DAO.
     */
    public MessageService() {
        this.messageDAO = new MessageDAO();
        this.webSocketService = WebSocketService.getInstance();
    }

    /**
     * Retrieves all messages for a specific chat.
     *
     * @param chatId The ID of the chat to get messages for
     * @return List of messages in the chat
     * @throws DataAccessException if there's an error accessing the database
     */
    @Override
    public List<Message> getMessagesByChatId(int chatId) {
        if (chatId <= 0) {
            throw new IllegalArgumentException("Chat ID must be a positive number");
        }
        
        try {
            return messageDAO.getByChatId(chatId);
        } catch (Exception e) {
            logger.error("Error retrieving messages for chat {}: {}", chatId, e.getMessage(), e);
            throw e; // Let the custom exceptions from DAO propagate
        }
    }

    /**
     * Asynchronously retrieves all messages for a specific chat.
     *
     * @param chatId The ID of the chat to get messages for
     * @return CompletableFuture that will provide the list of messages
     */
    @Override
    public CompletableFuture<List<Message>> getMessagesByChatIdAsync(int chatId) {
        if (chatId <= 0) {
            CompletableFuture<List<Message>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Chat ID must be a positive number"));
            return future;
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return messageDAO.getByChatId(chatId);
            } catch (Exception e) {
                logger.error("Error retrieving messages asynchronously for chat {}: {}", chatId, e.getMessage(), e);
                throw e; // Let the custom exceptions from DAO propagate
            }
        });
    }

    /**
     * Creates a new text message.
     *
     * @param content The text content of the message
     * @param chat The chat the message belongs to
     * @param user The user who sent the message
     * @throws DataCreationException if there's an error creating the message
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    @Override
    public void createTextMessage(String content, Chat chat, User user) {
        // Validate parameters
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (chat == null) {
            throw new IllegalArgumentException("Chat cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        try {
            Message message = new Message();
            message.setId(0);  // ID will be set by database
            message.setContent(content);
            message.setContentType(ContentType.TEXT);
            message.setPinned(false);
            message.setChat(chat);
            message.setSender(user);
            messageDAO.create(message);
        } catch (Exception e) {
            logger.error("Error creating text message: {}", e.getMessage(), e);
            if (!(e instanceof DataCreationException)) {
                throw new DataCreationException("Failed to create text message", e);
            }
            throw e;
        }
    }

    /**
     * Creates a new file message from a file.
     *
     * @param file The file to attach to the message
     * @param content Optional description for the file
     * @param chat The chat the message belongs to
     * @param user The user who sent the message
     * @throws IOException If there is an error reading the file
     */
    @Override
    public void createFileMessage(File file, String content, Chat chat, User user) throws IOException {
        // Read file data
        byte[] fileData = FileService.readFileBytes(file);
        
        // Get file details
        String fileName = file.getName();
        String fileExtension = FileService.getFileExtension(fileName);
        
        // Determine content type based on file extension
        ContentType contentType;
        if (FileService.isImageFile(fileExtension)) {
            contentType = ContentType.IMAGE;
        } else {
            contentType = ContentType.FILE;
        }
        
        // Create message content
        String messageContent = content.isEmpty() ? fileName : content;
        
        // Create and save message using Message object
        Message message = new Message();
        message.setId(0);  // ID will be set by database
        message.setContent(messageContent);
        message.setContentType(contentType);
        message.setFileData(fileData);
        message.setFileName(fileName);
        message.setFileType(fileExtension);
        message.setPinned(false);
        message.setChat(chat);
        message.setSender(user);

        messageDAO.create(message);
    }

    /**
     * Creates a new file message using a pre-configured Message object.
     *
     * @param file The file to attach to the message
     * @param message The pre-configured message object (should have sender, chat, content, etc. already set)
     * @throws IOException If there is an error reading the file
     */
    @Override
    public void createFileMessageWithData(File file, Message message) throws IOException {
        // Read file data
        byte[] fileData = FileService.readFileBytes(file);
        
        // Get file details
        String fileName = file.getName();
        String fileExtension = FileService.getFileExtension(fileName);
        
        // Determine content type based on file extension
        ContentType contentType;
        if (FileService.isImageFile(fileExtension)) {
            contentType = ContentType.IMAGE;
        } else {
            contentType = ContentType.FILE;
        }
        
        // Update the message with file information
        message.setContentType(contentType);
        message.setFileData(fileData);
        message.setFileName(fileName);
        message.setFileType(fileExtension);
        
        // If no content was provided, use filename
        if (message.getContent() == null || message.getContent().isEmpty()) {
            message.setContent(fileName);
        }

        messageDAO.create(message);
    }

    /**
     * Creates a file message with chunking support for large files.
     * This method is now simplified as chunking is no longer used.
     *
     * @param file The file to attach to the message
     * @param message The pre-configured message object (should have sender, chat, content, etc. already set)
     * @throws IOException If there is an error reading the file
     */
    @Override
    public void createFileMessageWithChunking(File file, Message message) throws IOException {
        // Simply forward to the regular file message creation method
        createFileMessageWithData(file, message);
    }

    /**
     * Creates a new message with the provided data or updates an existing one.
     *
     * @param message The message to create or update
     */
    @Override
    public Message createMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        try {
            // Create the message in database
            Message createdMessage = messageDAO.create(message);
            
            // Send the message over WebSocket if connected
            try {
                sendMessageOverWebSocket(createdMessage);
            } catch (WebSocketMessageException e) {
                // Log but don't fail the operation if WebSocket sending fails
                logger.warn("Failed to send message over WebSocket: {}", e.getMessage());
                // The message was created successfully in the database, so we don't throw an exception
            }
            
            return createdMessage;
        } catch (DataCreationException e) {
            // Just rethrow DAO exceptions
            logger.error("Failed to create message in database: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions
            logger.error("Unexpected error creating message: {}", e.getMessage(), e);
            throw new DataCreationException("Failed to create message due to an unexpected error", e);
        }
    }

    /**
     * Gets the last message in a chat.
     *
     * @param chatId The ID of the chat
     * @return The most recent message in the chat
     */
    @Override
    public Message getLastMessageByChatId(int chatId) {
        return messageDAO.getLastMessageByChatId(chatId);
    }

    /**
     * Marks a message as deleted in the database.
     * Note: This doesn't actually delete the message, just updates its content to indicate deletion.
     *
     * @param messageId The ID of the message to mark as deleted
     * @return true if update was successful, false otherwise
     */
    @Override
    public boolean markMessageAsDeleted(int messageId) {
        Message message = messageDAO.getById(messageId);
        if (message != null) {
            try {
                logger.debug("Marking message as deleted: id={}", messageId);
                message.setContent("[Message deleted]");
                // Set a flag to indicate deletion
                // Note: assuming Message has such a flag, otherwise use message metadata
                message.setPinned(false); // Unpin if deleted
                messageDAO.update(message);
                return true;
            } catch (Exception e) {
                logger.error("Error marking message as deleted: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Gets a message by its ID.
     *
     * @param messageId The ID of the message to retrieve
     * @return The message if found
     * @throws MessageNotFoundException if the message doesn't exist
     * @throws IllegalArgumentException if the message ID is invalid
     */
    @Override
    public Message getMessageById(int messageId) {
        if (messageId <= 0) {
            throw new IllegalArgumentException("Message ID must be a positive number");
        }
        
        try {
            return messageDAO.getById(messageId);
        } catch (MessageNotFoundException e) {
            // Just rethrow not found exceptions
            logger.warn("Message not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log and wrap other exceptions
            logger.error("Error retrieving message {}: {}", messageId, e.getMessage(), e);
            throw new DataAccessException("Failed to retrieve message with ID " + messageId, e);
        }
    }

    /**
     * Gets all pinned messages for a chat.
     *
     * @param chatId The ID of the chat
     * @return List of pinned messages in the chat
     */
    @Override
    public List<Message> getPinnedMessagesByChatId(int chatId) {
        List<Message> allMessages = messageDAO.getByChatId(chatId);
        List<Message> pinnedMessages = new ArrayList<>();
        
        for (Message message : allMessages) {
            if (message.isPinned()) {
                pinnedMessages.add(message);
            }
        }
        
        return pinnedMessages;
    }

    /**
     * Pins a message in a chat.
     *
     * @param messageId The ID of the message to pin
     * @return true if the operation was successful, false otherwise
     * @throws MessageNotFoundException if the message doesn't exist
     * @throws DataUpdateException if there's an error updating the message
     */
    @Override
    public boolean pinMessage(int messageId) {
        return pinToggleMessage(messageId, true);
    }

    /**
     * Unpins a message in a chat.
     *
     * @param messageId The ID of the message to unpin
     * @return true if the operation was successful, false otherwise
     * @throws MessageNotFoundException if the message doesn't exist
     * @throws DataUpdateException if there's an error updating the message
     */
    @Override
    public boolean unpinMessage(int messageId) {
        return pinToggleMessage(messageId, false);
    }
    
    /**
     * Helper method to toggle the pinned state of a message.
     *
     * @param messageId The ID of the message to update
     * @param pin true to pin the message, false to unpin it
     * @return true if the operation was successful, false otherwise
     * @throws MessageNotFoundException if the message doesn't exist
     * @throws DataUpdateException if there's an error updating the message
     */
    private boolean pinToggleMessage(int messageId, boolean pin) {
        try {
            // Get the message
            Message message = getMessageById(messageId);
            
            // Check if already in the desired state
            if (message.isPinned() == pin) {
                logger.debug("Message {} is already {}pinned", messageId, pin ? "" : "un");
                return true;
            }
            
            // Update the message
            message.setPinned(pin);
            messageDAO.update(message);
            
            // Send update via WebSocket if connected
            if (webSocketService != null && webSocketService.isConnected()) {
                webSocketService.sendMessage(message);
            }
            
            return true;
        } catch (MessageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error {}pinning message {}: {}", pin ? "" : "un", messageId, e.getMessage(), e);
            throw new DataUpdateException("Failed to " + (pin ? "pin" : "unpin") + " message", e);
        }
    }

    /**
     * Checks if a chat has any pinned messages.
     *
     * @param chatId The ID of the chat
     * @return true if the chat has pinned messages, false otherwise
     */
    @Override
    public boolean hasPinnedMessages(int chatId) {
        List<Message> pinnedMessages = getPinnedMessagesByChatId(chatId);
        return !pinnedMessages.isEmpty();
    }

    /**
     * Updates a message in the database.
     *
     * @param message The message to update
     */
    @Override
    public void updateMessage(Message message) {
        messageDAO.update(message);
    }

    /**
     * Deletes a message from the database.
     *
     * @param messageId The ID of the message to delete
     */
    @Override
    public void deleteMessage(int messageId) {
        if (messageId <= 0) {
            throw new IllegalArgumentException("Message ID must be a positive number");
        }
        
        try {
            // First get the message to be able to send WebSocket notification
            Message message = messageDAO.getById(messageId);
            
            // Delete message from database
            boolean deleted = messageDAO.delete(messageId);
            
            if (deleted && webSocketService != null && webSocketService.isConnected()) {
                // Send WebSocket notification if connected
                try {
                    webSocketService.deleteMessage(message);
                } catch (Exception e) {
                    // Log but don't fail if WebSocket sending fails
                    logger.warn("Failed to send message deletion notification over WebSocket: {}", e.getMessage());
                }
            }
        } catch (MessageNotFoundException e) {
            // Just rethrow specific exceptions
            logger.warn("Failed to delete message - not found: {}", e.getMessage());
            throw e;
        } catch (DataDeletionException e) {
            logger.error("Failed to delete message from database: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting message {}: {}", messageId, e.getMessage(), e);
            throw new DataDeletionException("Message", messageId, "An unexpected error occurred while deleting the message");
        }
    }

    /**
     * Sends a message over WebSocket.
     *
     * @param message The message to send
     * @throws WebSocketMessageException if there's an error sending the message
     * @throws IllegalArgumentException if the message is invalid
     */
    public void sendMessageOverWebSocket(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.getChat() == null) {
            throw new IllegalArgumentException("Message chat cannot be null");
        }
        
        try {
            if (webSocketService == null || !webSocketService.isConnected()) {
                throw new WebSocketMessageException(
                    WebSocketMessageException.ErrorType.SEND_FAILED,
                    "WebSocket service is not available or not connected"
                );
            }
            
            webSocketService.sendMessage(message);
        } catch (WebSocketMessageException e) {
            // Let WebSocketMessageException propagate as is
            throw e;
        } catch (Exception e) {
            logger.error("Error sending message over WebSocket: {}", e.getMessage(), e);
            throw new WebSocketMessageException(
                WebSocketMessageException.ErrorType.SEND_FAILED,
                "Failed to send message: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Retrieves the file data for a message from the database.
     * This is called when a file message is received via WebSocket.
     * 
     * @param messageId The ID of the message to retrieve file data for
     * @return The message with file data, or null if not found
     */
    public Message loadFileDataForMessage(int messageId) {
        logger.info("Loading file data for message: {}", messageId);
        return messageDAO.getById(messageId);
    }

    /**
     * Create WebSocket message DTO for the specified message
     * 
     * @param message The message to include in the DTO
     * @return The WebSocketMessageDTO
     */
    private WebSocketMessageDTO createWebSocketDTO(Message message) {
        WebSocketMessageDTO dto = new WebSocketMessageDTO();
        dto.setChatId(message.getChat().getId());
        dto.setMessage(message);
        
        // Set the current user
        User currentUser = message.getSender();
        if (currentUser != null) {
            dto.setUser(currentUser);
        }
        
        return dto;
    }
}
    
