package com.ap4.client.interfaces.services;

import java.util.List;

import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

/**
 * Interface for chat participant-related operations.
 * <p>
 * This interface defines the methods for adding, updating, and removing
 * participants from chats.
 * </p>
 */
public interface IChatParticipantService {

    /**
     * Gets all participants in a chat.
     *
     * @param chatId the ID of the chat
     * @return a list of chat participants
     */
    List<ChatParticipant> getParticipantsByChatId(int chatId);

    /**
     * Gets a specific participant by user ID and chat ID.
     *
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @return the chat participant if found; otherwise, {@code null}
     */
    ChatParticipant getParticipant(int userId, int chatId);

    /**
     * Gets all chats a user is participating in.
     *
     * @param userId the ID of the user
     * @return a list of chat participants for the user
     */
    List<ChatParticipant> getParticipantsByUserId(int userId);

    /**
     * Adds a user to a chat with the specified role.
     *
     * @param user the user to add
     * @param chat the chat to add the user to
     * @param role the role for the user in the chat
     * @return the newly created chat participant
     */
    ChatParticipant addParticipant(User user, Chat chat, Role role);

    /**
     * Updates the role of a participant in a chat.
     *
     * @param userId  the ID of the user whose role is to be updated
     * @param chatId  the ID of the chat in which to update the role
     * @param newRole the new role for the user
     * @return {@code true} if the update is successful; {@code false} otherwise
     */
    boolean updateParticipantRole(int userId, int chatId, Role newRole);

    /**
     * Removes a user from a chat.
     *
     * @param userId the ID of the user to remove
     * @param chatId the ID of the chat from which to remove the user
     * @return {@code true} if the removal is successful; {@code false} otherwise
     */
    boolean removeParticipant(int userId, int chatId);

    /**
     * Checks if a user has a specific role in a chat.
     *
     * @param userId the ID of the user
     * @param chatId the ID of the chat
     * @param role   the role to check for
     * @return {@code true} if the user has the specified role; {@code false} otherwise
     */
    boolean hasRole(int userId, int chatId, Role role);

    /**
     * Checks if a user has permission to perform an action in a chat.
     * <p>
     * The permission is determined by comparing the user's role hierarchy with the required role.
     * </p>
     *
     * @param userId       the ID of the user
     * @param chatId       the ID of the chat
     * @param requiredRole the minimum role required for the action
     * @return {@code true} if the user has sufficient permissions; {@code false} otherwise
     */
    boolean hasPermission(int userId, int chatId, Role requiredRole);

    /**
     * Finds a chat participant by user ID and chat ID in a list of participants.
     *
     * @param participants the list of chat participants to search
     * @param userId the ID of the user to find
     * @return the chat participant if found; otherwise, {@code null}
     */
    ChatParticipant findParticipantInList(List<ChatParticipant> participants, int userId);

    /**
     * Validates if a role change is allowed based on business rules.
     * Checks if:
     * - The current user has permission to change roles
     * - The current user is not changing their own role
     * - Only OWNER can promote to OWNER or demote another OWNER
     *
     * @param chatId the ID of the chat
     * @param currentUserId the ID of the user making the change
     * @param targetUserId the ID of the user whose role is being changed
     * @param newRole the new role to assign
     * @param currentRole the current role of the target user
     * @return a validation result string; null if validation passes, otherwise an error message
     */
    String validateRoleChange(int chatId, int currentUserId, int targetUserId, Role newRole, Role currentRole);
}
