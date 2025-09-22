package com.ap4.client.services;

import com.ap4.client.db.dao.ChatParticipantDAO;
import com.ap4.client.exceptions.data.ChatParticipantNotFoundException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.interfaces.db.dao.IChatParticipantDAO;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service class for chat participant-related operations.
 * Handles adding, updating, and removing participants from chats.
 */
public class ChatParticipantService implements IChatParticipantService {
    private static final Logger logger = LogManager.getLogger(ChatParticipantService.class);
    private final IChatParticipantDAO participantDAO;

    /**
     * Constructor with dependency injection for the DAO.
     */
    public ChatParticipantService() {
        this.participantDAO = new ChatParticipantDAO();
    }

    /**
     * Gets all participants in a chat.
     *
     * @param chatId The ID of the chat
     * @return List of chat participants
     */
    public List<ChatParticipant> getParticipantsByChatId(int chatId) {
        return participantDAO.getByChatId(chatId);
    }

    /**
     * Gets a specific participant by user ID and chat ID.
     *
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @return The chat participant if found, null otherwise
     */
    public ChatParticipant getParticipant(int userId, int chatId) {
        return participantDAO.getByUserIdAndChatId(userId, chatId);
    }

    /**
     * Gets all chats a user is participating in.
     *
     * @param userId The ID of the user
     * @return List of chat participants for the user
     */
    public List<ChatParticipant> getParticipantsByUserId(int userId) {
        return participantDAO.getByUserId(userId);
    }

    /**
     * Adds a user to a chat with the specified role.
     *
     * @param user The user to add
     * @param chat The chat to add the user to
     * @param role The role for the user in the chat
     * @return The newly created chat participant
     * @throws InvalidDataException if any of the parameters are invalid
     * @throws DuplicateKeyException if the user is already a participant in the chat
     * @throws EntityRelationshipException if the user or chat doesn't exist
     * @throws DataCreationException if there's an error creating the participant
     */
    public ChatParticipant addParticipant(User user, Chat chat, Role role) {
        if (user == null) {
            throw new InvalidDataException("ChatParticipant", "user", "cannot be null");
        }
        
        if (chat == null) {
            throw new InvalidDataException("ChatParticipant", "chat", "cannot be null");
        }
        
        if (role == null) {
            throw new InvalidDataException("ChatParticipant", "role", "cannot be null");
        }
        
        // Check if participant already exists
        ChatParticipant existingParticipant = participantDAO.getByUserIdAndChatId(user.getId(), chat.getId());
        if (existingParticipant != null) {
            throw new DuplicateKeyException("ChatParticipant", "user_id and chat_id", 
                    "User is already a participant in this chat");
        }
        
        try {
            ChatParticipant participant = new ChatParticipant();
            participant.setUser(user);
            participant.setChat(chat);
            participant.setRole(role);
            
            ChatParticipant created = participantDAO.create(participant);
            logger.info("Added user {} (ID: {}) to chat {} (ID: {}) with role {}", 
                    user.getUsername(), user.getId(), chat.getName(), chat.getId(), role);
            return created;
        } catch (DuplicateKeyException | EntityRelationshipException | InvalidDataException e) {
            // Rethrow specific exceptions from DAO
            logger.warn("Failed to add participant - validation error: {}", e.getMessage());
            throw e;
        } catch (DataCreationException e) {
            logger.error("Failed to create participant: {}", e.getMessage(), e);
            throw e;
        } catch (DataAccessException e) {
            logger.error("Data access error while adding participant: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error adding participant: {}", e.getMessage(), e);
            throw new DataCreationException("Failed to add participant due to an unexpected error", e);
        }
    }

    /**
     * Updates the role of a participant in a chat.
     *
     * @param userId The ID of the user whose role to update
     * @param chatId The ID of the chat in which to update the role
     * @param newRole The new role for the user
     * @return true if successful, false otherwise
     * @throws ChatParticipantNotFoundException if the participant doesn't exist
     * @throws InvalidDataException if any of the parameters are invalid
     * @throws DataUpdateException if there's an error updating the participant
     */
    public boolean updateParticipantRole(int userId, int chatId, Role newRole) {
        if (userId <= 0) {
            logger.warn("Invalid userId provided: {}", userId);
            throw new InvalidDataException("ChatParticipant", "userId", "must be a positive number");
        }
        
        if (chatId <= 0) {
            logger.warn("Invalid chatId provided: {}", chatId);
            throw new InvalidDataException("ChatParticipant", "chatId", "must be a positive number");
        }
        
        if (newRole == null) {
            logger.warn("Invalid role provided: null");
            throw new InvalidDataException("ChatParticipant", "role", "cannot be null");
        }
        
        try {
            logger.debug("Looking up participant with userId={}, chatId={}", userId, chatId);
            
            ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
            
            if (participant == null) {
                logger.warn("Participant not found for userId={}, chatId={}", userId, chatId);
                throw new ChatParticipantNotFoundException(userId, chatId);
            }
            
            // Verify the chat IDs match properly
            if (participant.getChat() != null && participant.getChat().getId() != chatId) {
                logger.error("Chat ID mismatch in participant data. Expected: {}, Found: {}", 
                    chatId, participant.getChat().getId());
                throw new InvalidDataException("ChatParticipant", "chatId", 
                    "Chat ID mismatch between request and database entity");
            }
            
            // Only update if the role actually changed
            if (participant.getRole() != newRole) {
                logger.debug("Updating role from {} to {} for userId={}, chatId={}", 
                    participant.getRole(), newRole, userId, chatId);
                
                participant.setRole(newRole);
                participantDAO.update(participant);
                logger.info("Updated role for user ID {} in chat ID {} to {}", userId, chatId, newRole);
            } else {
                logger.debug("Role already set to {} for user ID {} in chat ID {}", newRole, userId, chatId);
            }
            
            return true;
        } catch (ChatParticipantNotFoundException e) {
            // Rethrow specific exceptions
            logger.warn("Failed to update participant role - not found: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.warn("Failed to update participant role - invalid data: {}", e.getMessage());
            throw e;
        } catch (DataUpdateException e) {
            logger.error("Failed to update participant role in database: {}", e.getMessage(), e);
            throw e;
        } catch (DataAccessException e) {
            logger.error("Data access error while updating participant role: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating participant role: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to update participant role due to an unexpected error", e);
        }
    }

    /**
     * Removes a user from a chat.
     *
     * @param userId The ID of the user to remove
     * @param chatId The ID of the chat to remove the user from
     * @return true if successful, false otherwise
     * @throws ChatParticipantNotFoundException if the participant doesn't exist
     * @throws InvalidDataException if any of the parameters are invalid
     * @throws DataDeletionException if there's an error deleting the participant
     */
    public boolean removeParticipant(int userId, int chatId) {
        if (userId <= 0) {
            throw new InvalidDataException("ChatParticipant", "userId", "must be a positive number");
        }
        
        if (chatId <= 0) {
            throw new InvalidDataException("ChatParticipant", "chatId", "must be a positive number");
        }
        
        try {
            ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
            if (participant == null) {
                throw new ChatParticipantNotFoundException(userId, chatId);
            }
            
            boolean deleted = participantDAO.delete(participant.getId());
            if (deleted) {
                logger.info("Removed user ID {} from chat ID {}", userId, chatId);
            } else {
                logger.warn("Failed to remove user ID {} from chat ID {} - database deletion failed", userId, chatId);
                throw new DataDeletionException("ChatParticipant", participant.getId(), "Database deletion operation failed");
            }
            
            return true;
        } catch (ChatParticipantNotFoundException e) {
            // Rethrow specific exceptions
            logger.warn("Failed to remove participant - not found: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            logger.warn("Failed to remove participant - invalid data: {}", e.getMessage());
            throw e;
        } catch (DataDeletionException e) {
            logger.error("Failed to delete participant from database: {}", e.getMessage(), e);
            throw e;
        } catch (DataAccessException e) {
            logger.error("Data access error while removing participant: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error removing participant: {}", e.getMessage(), e);
            throw new DataDeletionException("ChatParticipant", -1, "An unexpected error occurred while removing the participant");
        }
    }

    /**
     * Checks if a user has a specific role in a chat.
     *
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @param role The role to check for
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(int userId, int chatId, Role role) {
        ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
        return participant != null && participant.getRole() == role;
    }

    /**
     * Checks if a user has permission to perform an action in a chat.
     *
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @param requiredRole The minimum role required for the action
     * @return true if the user has sufficient permissions, false otherwise
     */
    public boolean hasPermission(int userId, int chatId, Role requiredRole) {
        ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
        if (participant == null) {
            return false;
        }
        
        // Compare role ordinals to determine hierarchy
        return participant.getRole().ordinal() >= requiredRole.ordinal();
    }

    /**
     * Finds a chat participant by user ID in a list of participants.
     *
     * @param participants The list of chat participants to search
     * @param userId The ID of the user to find
     * @return The chat participant if found, null otherwise
     */
    @Override
    public ChatParticipant findParticipantInList(List<ChatParticipant> participants, int userId) {
        if (participants == null) {
            logger.warn("Cannot find participant: participants list is null");
            return null;
        }
        
        if (participants.isEmpty()) {
            logger.warn("Cannot find participant: participants list is empty");
            return null;
        }
        
        if (userId <= 0) {
            logger.warn("Cannot find participant: invalid userId {}", userId);
            return null;
        }
        
        logger.debug("Searching for participant with userId {} in list of {} participants", userId, participants.size());
        
        // First try with stream for cleaner code
        ChatParticipant found = participants.stream()
            .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
            .findFirst()
            .orElse(null);
            
        if (found != null) {
            logger.debug("Found participant with userId {} in list, role: {}", userId, found.getRole());
            return found;
        }
        
        // If nothing found, do a verbose loop with detailed logging
        logger.debug("Participant not found with stream, attempting manual search with detailed logging");
        for (int i = 0; i < participants.size(); i++) {
            ChatParticipant p = participants.get(i);
            if (p == null) {
                logger.warn("Null participant at index {}", i);
                continue;
            }
            
            User user = p.getUser();
            if (user == null) {
                logger.warn("Participant at index {} has null user", i);
                continue;
            }
            
            int id = user.getId();
            logger.debug("Checking participant at index {}: userId={}, expected={}, match={}", 
                i, id, userId, id == userId);
                
            if (id == userId) {
                logger.debug("Found participant with userId {} at index {}, role: {}", userId, i, p.getRole());
                return p;
            }
        }
        
        logger.warn("Participant with userId {} not found in list after detailed search", userId);
        return null;
    }
    
    /**
     * Validates if a role change is allowed based on business rules.
     *
     * @param chatId The ID of the chat
     * @param currentUserId The ID of the user making the change
     * @param targetUserId The ID of the user whose role is being changed
     * @param newRole The new role to assign
     * @param currentRole The current role of the target user
     * @return null if validation passes, otherwise an error message
     */
    @Override
    public String validateRoleChange(int chatId, int currentUserId, int targetUserId, Role newRole, Role currentRole) {
        // Check if current user has permission (ADMIN or OWNER)
        ChatParticipant currentUserParticipant = participantDAO.getByUserIdAndChatId(currentUserId, chatId);
        if (currentUserParticipant == null) {
            return "Your membership information is not available.";
        }
        
        Role currentUserRole = currentUserParticipant.getRole();
        
        // Check if current user has permission to edit roles (must be ADMIN or OWNER)
        if (currentUserRole != Role.OWNER && currentUserRole != Role.ADMIN) {
            return "You don't have permission to change roles.";
        }
        
        // Don't allow changing your own role
        if (currentUserId == targetUserId) {
            return "You cannot change your own role.";
        }
        
        // Only OWNER can promote to OWNER or demote another OWNER
        if ((newRole == Role.OWNER || currentRole == Role.OWNER) && currentUserRole != Role.OWNER) {
            return "Only the owner can manage owner roles.";
        }
        
        // All validations passed
        return null;
    }
}
