package com.ap4.client.interfaces.db.dao;

import com.ap4.common.models.ChatParticipant;
import com.ap4.common.enums.Role;

import java.util.List;

/**
 * Interface for ChatParticipant entity data access operations.
 * Defines specific methods for ChatParticipant entity beyond the standard CRUD operations.
 */
public interface IChatParticipantDAO extends IDAO<ChatParticipant, Integer> {
    
    /**
     * Retrieves all participants in a specific chat.
     * 
     * @param chatId The ID of the chat
     * @return A list of participants in the chat
     */
    List<ChatParticipant> getByChatId(int chatId);
    
    /**
     * Retrieves all chat participations for a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of chat participations for the user
     */
    List<ChatParticipant> getByUserId(int userId);
    
    /**
     * Retrieves a specific chat participation by user and chat IDs.
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return The chat participation if found, null otherwise
     */
    ChatParticipant getByUserAndChatId(int userId, int chatId);
    
    /**
     * Updates the role of a user in a chat.
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @param newRole The new role for the user
     * @return true if the role was updated, false otherwise
     */
    boolean updateRole(int userId, int chatId, Role newRole);
    
    /**
     * Removes a user from a chat.
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return true if the user was removed, false otherwise
     */
    boolean removeParticipant(int userId, int chatId);
    
    /**
     * Checks if a user has a specific role or higher in a chat.
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @param requiredRole The minimum role required
     * @return true if the user has the required role or higher, false otherwise
     */
    boolean hasPermission(int userId, int chatId, Role requiredRole);
    
    /**
     * Retrieves all users with a specific role in a chat.
     * 
     * @param chatId The ID of the chat
     * @param role The role to filter by
     * @return A list of chat participants with the specified role
     */
    List<ChatParticipant> getByRole(int chatId, Role role);

    /**
     * Retrieves a specific chat participation by user and chat IDs.
     * 
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return The chat participation if found, null otherwise
     */
    ChatParticipant getByUserIdAndChatId(int userId, int chatId);

    /**
     * Deletes a chat participation by ID.
     * 
     * @param id The ID of the chat participation
     * @return true if the chat participation was deleted, false otherwise
     */
    boolean deleteById(int id);
}
