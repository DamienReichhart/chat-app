package com.ap4.client.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.db.dao.IChatParticipantDAO;
import com.ap4.client.db.dao.ChatParticipantDAO;
import com.ap4.client.interfaces.services.IChatPermissionService;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

/**
 * Implementation of IChatPermissionService that centralizes permission and role checking.
 * This service handles all aspects of determining user permissions within chat contexts.
 */
public class ChatPermissionService implements IChatPermissionService {
    private static final Logger logger = LogManager.getLogger(ChatPermissionService.class);
    
    private final IChatParticipantService chatParticipantService;
    private final IChatParticipantDAO participantDAO;
    
    /**
     * Constructor with explicit dependency injection for testing.
     * 
     * @param chatParticipantService The chat participant service to use
     */
    public ChatPermissionService(IChatParticipantService chatParticipantService) {
        this.chatParticipantService = chatParticipantService;
        this.participantDAO = new ChatParticipantDAO();
    }
    
    /**
     * Default constructor that creates necessary dependencies.
     */
    public ChatPermissionService() {
        this.chatParticipantService = new ChatParticipantService();
        this.participantDAO = new ChatParticipantDAO();
    }
    
    @Override
    public boolean canEditRoles(int userId, int chatId) {
        logger.debug("Checking if user ID {} can edit roles in chat ID {}", userId, chatId);
        
        if (userId <= 0 || chatId <= 0) {
            logger.warn("Invalid user ID or chat ID: userId={}, chatId={}", userId, chatId);
            return false;
        }
        
        try {
            // Get participant directly from the database
            ChatParticipant participant = chatParticipantService.getParticipant(userId, chatId);
            
            if (participant == null) {
                logger.warn("User ID {} is not a participant in chat ID {}", userId, chatId);
                return false;
            }
            
            Role role = participant.getRole();
            // Only OWNER can edit roles, not ADMIN
            boolean canEdit = (role == Role.OWNER);
            
            logger.info("User ID {} with role {} can {} edit roles in chat ID {}", 
                userId, role, canEdit ? "" : "not", chatId);
                
            return canEdit;
        } catch (Exception e) {
            logger.error("Error checking if user can edit roles: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canEditRoles(User user, Chat chat) {
        if (user == null || chat == null) {
            logger.warn("Cannot check permissions: user or chat is null");
            return false;
        }
        
        return canEditRoles(user.getId(), chat.getId());
    }
    
    @Override
    public Role getUserRole(int userId, int chatId) {
        logger.debug("Getting role for user ID {} in chat ID {}", userId, chatId);
        
        if (userId <= 0 || chatId <= 0) {
            logger.warn("Invalid user ID or chat ID: userId={}, chatId={}", userId, chatId);
            return null;
        }
        
        try {
            ChatParticipant participant = chatParticipantService.getParticipant(userId, chatId);
            
            if (participant == null) {
                logger.warn("User ID {} is not a participant in chat ID {}", userId, chatId);
                return null;
            }
            
            Role role = participant.getRole();
            logger.debug("User ID {} has role {} in chat ID {}", userId, role, chatId);
            
            return role;
        } catch (Exception e) {
            logger.error("Error getting user role: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Role getUserRole(User user, Chat chat) {
        if (user == null || chat == null) {
            logger.warn("Cannot get role: user or chat is null");
            return null;
        }
        
        return getUserRole(user.getId(), chat.getId());
    }
    
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
    
    @Override
    public String validateRoleChange(int chatId, int currentUserId, int targetUserId, Role newRole, Role currentRole) {
        // Check if current user has permission (ADMIN or OWNER)
        ChatParticipant currentUserParticipant = participantDAO.getByUserIdAndChatId(currentUserId, chatId);
        if (currentUserParticipant == null) {
            logger.warn("User ID {} is not a participant in chat ID {}", currentUserId, chatId);
            return "Your membership information is not available.";
        }
        
        Role currentUserRole = currentUserParticipant.getRole();
        logger.debug("User ID {} has role {} in chat ID {}", currentUserId, currentUserRole, chatId);
        
        // Check if current user has permission to edit roles (must be OWNER)
        if (currentUserRole != Role.OWNER) {
            logger.warn("User ID {} with role {} cannot change roles", currentUserId, currentUserRole);
            return "You don't have permission to change roles.";
        }
        
        // Don't allow changing your own role
        if (currentUserId == targetUserId) {
            logger.warn("User ID {} attempted to change their own role", currentUserId);
            return "You cannot change your own role.";
        }
        
        // Only OWNER can promote to OWNER or demote another OWNER
        if ((newRole == Role.OWNER || currentRole == Role.OWNER) && currentUserRole != Role.OWNER) {
            logger.warn("User ID {} with role {} cannot manage owner roles", currentUserId, currentUserRole);
            return "Only the owner can manage owner roles.";
        }
        
        // All validations passed
        logger.debug("Role change validation passed for user ID {} changing user ID {} from {} to {}", 
            currentUserId, targetUserId, currentRole, newRole);
        return null;
    }
    
    @Override
    public boolean hasRole(int userId, int chatId, Role role) {
        if (userId <= 0 || chatId <= 0 || role == null) {
            logger.warn("Invalid parameters for hasRole: userId={}, chatId={}, role={}", 
                userId, chatId, role);
            return false;
        }
        
        try {
            ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
            boolean hasRole = participant != null && participant.getRole() == role;
            
            logger.debug("User ID {} {} role {} in chat ID {}", 
                userId, hasRole ? "has" : "does not have", role, chatId);
                
            return hasRole;
        } catch (Exception e) {
            logger.error("Error checking if user has role: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean hasPermission(int userId, int chatId, Role requiredRole) {
        if (userId <= 0 || chatId <= 0 || requiredRole == null) {
            logger.warn("Invalid parameters for hasPermission: userId={}, chatId={}, requiredRole={}", 
                userId, chatId, requiredRole);
            return false;
        }
        
        try {
            ChatParticipant participant = participantDAO.getByUserIdAndChatId(userId, chatId);
            
            if (participant == null) {
                logger.warn("User ID {} is not a participant in chat ID {}", userId, chatId);
                return false;
            }
            
            // Compare role ordinals to determine hierarchy
            boolean hasPermission = participant.getRole().ordinal() >= requiredRole.ordinal();
            
            logger.debug("User ID {} with role {} {} permission for required role {} in chat ID {}", 
                userId, participant.getRole(), hasPermission ? "has" : "does not have", requiredRole, chatId);
                
            return hasPermission;
        } catch (Exception e) {
            logger.error("Error checking if user has permission: {}", e.getMessage(), e);
            return false;
        }
    }
} 