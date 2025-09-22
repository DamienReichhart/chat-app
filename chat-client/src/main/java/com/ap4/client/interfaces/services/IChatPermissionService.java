package com.ap4.client.interfaces.services;

import java.util.List;

import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

/**
 * Interface for checking and managing chat permissions and user roles.
 * This service centralizes all permission-related logic for chat participants.
 */
public interface IChatPermissionService {

    /**
     * Checks if a user can edit roles in a chat.
     * 
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @return true if the user can edit roles, false otherwise
     */
    boolean canEditRoles(int userId, int chatId);
    
    /**
     * Checks if a user can edit roles in a chat.
     * 
     * @param user the user
     * @param chat the chat
     * @return true if the user can edit roles, false otherwise
     */
    boolean canEditRoles(User user, Chat chat);
    
    /**
     * Gets a user's role in a chat.
     * 
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @return the user's role, or null if they are not a participant
     */
    Role getUserRole(int userId, int chatId);
    
    /**
     * Gets a user's role in a chat.
     * 
     * @param user the user
     * @param chat the chat
     * @return the user's role, or null if they are not a participant
     */
    Role getUserRole(User user, Chat chat);
    
    /**
     * Finds a chat participant with the specified user ID in a list of participants.
     * 
     * @param participants the list of participants
     * @param userId the ID of the user to find
     * @return the participant, or null if not found
     */
    ChatParticipant findParticipantInList(List<ChatParticipant> participants, int userId);
    
    /**
     * Validates if a role change is allowed based on business rules.
     * 
     * @param chatId the ID of the chat
     * @param currentUserId the ID of the user making the change
     * @param targetUserId the ID of the user whose role is being changed
     * @param newRole the new role to assign
     * @param currentRole the current role of the target user
     * @return null if validation passes, otherwise an error message
     */
    String validateRoleChange(int chatId, int currentUserId, int targetUserId, Role newRole, Role currentRole);
    
    /**
     * Checks if a user has a specific role in a chat.
     * 
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @param role the role to check for
     * @return true if the user has the specified role, false otherwise
     */
    boolean hasRole(int userId, int chatId, Role role);
    
    /**
     * Checks if a user has permission to perform an action in a chat.
     * 
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @param requiredRole the minimum role required for the action
     * @return true if the user has sufficient permissions, false otherwise
     */
    boolean hasPermission(int userId, int chatId, Role requiredRole);
} 