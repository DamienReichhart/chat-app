package com.ap4.client.interfaces.db.dao;

import com.ap4.common.models.Message;

import java.util.Date;
import java.util.List;

/**
 * Interface for Message entity data access operations.
 * Defines specific methods for Message entity beyond the standard CRUD operations.
 */
public interface IMessageDAO extends IDAO<Message, Integer> {
    
    /**
     * Retrieves all messages in a specific chat.
     * 
     * @param chatId The ID of the chat
     * @return A list of messages in the chat
     */
    List<Message> getByChatId(int chatId);
    
    /**
     * Retrieves all messages sent by a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of messages sent by the user
     */
    List<Message> getByUserId(int userId);
    
    /**
     * Retrieves messages in a chat with pagination.
     * 
     * @param chatId The ID of the chat
     * @param limit The maximum number of messages to retrieve
     * @param offset The offset for pagination
     * @return A list of messages in the chat
     */
    List<Message> getByChatIdPaginated(int chatId, int limit, int offset);
    
    /**
     * Retrieves messages in a chat created after a specific date.
     * 
     * @param chatId The ID of the chat
     * @param date The date after which messages were created
     * @return A list of messages in the chat created after the date
     */
    List<Message> getByChatIdAfterDate(int chatId, Date date);
    
    /**
     * Retrieves the latest message in a chat.
     * 
     * @param chatId The ID of the chat
     * @return The latest message in the chat
     */
    Message getLatestByChatId(int chatId);
    
    /**
     * Retrieves the count of unread messages for a user in a chat.
     * 
     * @param chatId The ID of the chat
     * @param userId The ID of the user
     * @return The count of unread messages
     */
    int getUnreadCount(int chatId, int userId);
    
    /**
     * Marks all messages in a chat as read for a user.
     * 
     * @param chatId The ID of the chat
     * @param userId The ID of the user
     * @return The number of messages marked as read
     */
    int markAllAsRead(int chatId, int userId);
    
    /**
     * Searches for messages containing specific text.
     * 
     * @param chatId The ID of the chat to search in
     * @param searchText The text to search for
     * @return A list of messages containing the search text
     */
    List<Message> searchByContent(int chatId, String searchText);

    /**
     * Retrieves the last message in a chat.
     * 
     * @param chatId The ID of the chat
     * @return The last message in the chat
     */
    Message getLastMessageByChatId(int chatId);
}
