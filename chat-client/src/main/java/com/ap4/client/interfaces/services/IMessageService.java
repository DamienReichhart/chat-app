package com.ap4.client.interfaces.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ap4.common.models.Chat;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Interface for message-related operations.
 * <p>
 * This interface defines methods for the creation, retrieval, and management of messages.
 * </p>
 */
public interface IMessageService {

    /**
     * Retrieves all messages for a specific chat.
     *
     * @param chatId the ID of the chat to get messages for
     * @return a list of messages in the chat
     */
    List<Message> getMessagesByChatId(int chatId);

    /**
     * Asynchronously retrieves all messages for a specific chat.
     *
     * @param chatId the ID of the chat to get messages for
     * @return a {@link CompletableFuture} that will provide the list of messages
     */
    CompletableFuture<List<Message>> getMessagesByChatIdAsync(int chatId);

    /**
     * Creates a new text message.
     *
     * @param content the text content of the message
     * @param chat    the chat the message belongs to
     * @param user    the user who sent the message
     */
    void createTextMessage(String content, Chat chat, User user);

    /**
     * Creates a new file message from a file.
     *
     * @param file    the file to attach to the message
     * @param content optional description for the file
     * @param chat    the chat the message belongs to
     * @param user    the user who sent the message
     * @throws IOException if there is an error reading the file
     */
    void createFileMessage(File file, String content, Chat chat, User user) throws IOException;

    /**
     * Creates a new file message using a pre-configured Message object.
     *
     * @param file    the file to attach to the message
     * @param message the pre-configured message object (should have sender, chat, content, etc. already set)
     * @throws IOException if there is an error reading the file
     */
    void createFileMessageWithData(File file, Message message) throws IOException;

    /**
     * Creates a file message with chunking support for large files.
     * This method will break the file into smaller chunks for transmission
     * if it exceeds the maximum message size.
     *
     * @param file    the file to attach to the message
     * @param message the pre-configured message object (should have sender, chat, content, etc. already set)
     * @throws IOException if there is an error reading the file
     */
    void createFileMessageWithChunking(File file, Message message) throws IOException;

    /**
     * Creates a new message with the provided data.
     *
     * @param message the message to create
     */
    Message createMessage(Message message);

    /**
     * Gets the last message in a chat.
     *
     * @param chatId the ID of the chat
     * @return the most recent message in the chat
     */
    Message getLastMessageByChatId(int chatId);

    /**
     * Marks a message as deleted in the database.
     * <p>
     * Note: This doesn't actually delete the message, it just updates its deleted flag.
     * </p>
     *
     * @param messageId the ID of the message to mark as deleted
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean markMessageAsDeleted(int messageId);

    /**
     * Pins a message in a chat.
     *
     * @param messageId the ID of the message to pin
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean pinMessage(int messageId);

    /**
     * Unpins a message in a chat.
     *
     * @param messageId the ID of the message to unpin
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean unpinMessage(int messageId);

    /**
     * Gets all pinned messages for a chat.
     *
     * @param chatId the ID of the chat
     * @return a list of pinned messages in the chat
     */
    List<Message> getPinnedMessagesByChatId(int chatId);

    /**
     * Checks if a chat has any pinned messages.
     *
     * @param chatId the ID of the chat
     * @return {@code true} if the chat has pinned messages; {@code false} otherwise
     */
    boolean hasPinnedMessages(int chatId);

    /**
     * Retrieves a message by its ID.
     *
     * @param messageId The ID of the message to retrieve
     * @return The message with the specified ID, or null if it doesn't exist
     */
    Message getMessageById(int messageId);

    /**
     * Updates a message in the database.
     *
     * @param message The message to update
     */
    void updateMessage(Message message);

    /**
     * Deletes a message from the database.
     *
     * @param messageId The ID of the message to delete
     */
    void deleteMessage(int messageId);
    
    /**
     * Sends a message to the WebSocket server.
     * This is used primarily for initial sending of messages.
     * For file messages, only the metadata is sent; recipients load the actual file data from the database.
     *
     * @param message The message to send
     */
    void sendMessageOverWebSocket(Message message);
}
