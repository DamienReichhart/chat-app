package com.ap4.client.interfaces.db.dao;

import java.util.List;

import com.ap4.common.enums.ChatType;
import com.ap4.common.models.Chat;

/**
 * Interface for Chat entity data access operations.
 * Defines specific methods for Chat entity beyond the standard CRUD operations.
 */
public interface IChatDAO extends IDAO<Chat, Integer> {
    
    /**
     * Retrieves a chat by its name.
     * 
     * @param chatName The name of the chat to retrieve
     * @return The chat if found, null otherwise
     */
    Chat getByName(String chatName);
    
    /**
     * Retrieves all chats available in the system.
     * 
     * @return A list of all chats
     */
    List<Chat> getAll();
    
    /**
     * Retrieves all chats of a specific type.
     * 
     * @param chatType The type of chats to retrieve
     * @return A list of chats of the specified type
     */
    List<Chat> getByChatType(ChatType chatType);
    
    /**
     * Retrieves all chats that a user participates in.
     * 
     * @param userId The ID of the user
     * @return A list of chats the user participates in
     */
    List<Chat> getByParticipantId(int userId);
    
    /**
     * Counts the number of chats with the given name.
     * Useful for checking if a chat name is already in use.
     * 
     * @param chatName The chat name to check
     * @return The number of chats with the given name
     */
    int countByChatName(String chatName);
    
    /**
     * Deletes a chat by its name.
     * 
     * @param chatName The name of the chat to delete
     * @return true if the chat was deleted, false otherwise
     */
    boolean deleteByChatName(String chatName);
    
    /**
     * Searches for chats by name pattern.
     * 
     * @param namePattern The pattern to search for in chat names
     * @return A list of chats matching the pattern
     */
    List<Chat> searchByNamePattern(String namePattern);
    
    /**
     * Gets chats of a specific type that a user is not a member of.
     *
     * @param chatType The type of chat to filter by
     * @param userId The ID of the user 
     * @return List of chats of the specified type that the user is not a member of
     */
    List<Chat> getChatsByTypeNotJoinedByUser(ChatType chatType, int userId);
    
    /**
     * Gets all public chats (CHANEL and GROUP types) that are not marked as deleted.
     * 
     * @return A list of all public chats
     */
    List<Chat> getPublicChats();
    
    /**
     * Searches for public chats of a specific type with name or description containing the query.
     *
     * @param chatType The type of chat to filter by
     * @param query The search query for name or description
     * @return List of matching chats
     */
    List<Chat> searchPublicChatsByTypeAndQuery(ChatType chatType, String query);
    
    /**
     * Searches for public chats of a specific type that a user is not a member of,
     * with name or description containing the query.
     *
     * @param chatType The type of chat to filter by
     * @param userId The ID of the user
     * @param query The search query for name or description
     * @return List of matching chats the user has not joined
     */
    List<Chat> searchPublicChatsByTypeNotJoinedByUser(ChatType chatType, int userId, String query);
}
