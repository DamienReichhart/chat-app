package com.ap4.client.services;

import com.ap4.client.interfaces.services.IAuthorizationService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.User;
import com.ap4.common.models.ChatParticipant;
import com.ap4.client.interfaces.services.IChatParticipantService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Implementation of the authorization service that handles permission checks based on user roles.
 * This service determines if users have permission to perform specific actions
 * on chats based on their roles within those chats.
 */
public class AuthorizationService implements IAuthorizationService {
    private static final Logger logger = LogManager.getLogger(AuthorizationService.class);
    
    private final IChatParticipantService chatParticipantService;
    
    /**
     * Constructor with dependency injection
     * 
     * @param chatParticipantService Service to access chat participant information
     */
    public AuthorizationService(IChatParticipantService chatParticipantService) {
        this.chatParticipantService = chatParticipantService;
    }
    
    /**
     * Default constructor
     */
    public AuthorizationService() {
        this.chatParticipantService = new ChatParticipantService();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canEditChat(Chat chat, User user) {
        if (chat == null || user == null) {
            logger.warn("Cannot check edit permission: chat or user is null");
            return false;
        }
        
        // Check if chat type is editable
        if (!isChatTypeEditable(chat.getChatType())) {
            return false;
        }
        
        // Check if user is a participant in the chat
        List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(chat.getId());
        
        // Find the user's participant record
        for (ChatParticipant participant : participants) {
            if (participant.getUser().getId() == user.getId()) {
                // Check if user is an admin or owner
                Role role = participant.getRole();
                return role == Role.ADMIN || role == Role.OWNER;
            }
        }
        
        // User not found in participants
        logger.info("User {} is not a participant in chat {}", user.getId(), chat.getId());
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeleteChat(Chat chat, User user) {
        if (chat == null || user == null) {
            logger.warn("Cannot check delete permission: chat or user is null");
            return false;
        }
        
        // Any participant can leave a chat
        List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(chat.getId());
        
        for (ChatParticipant participant : participants) {
            if (participant.getUser().getId() == user.getId()) {
                // Check if INDIVIDUAL chat type - special rules may apply
                if (chat.getChatType() == ChatType.INDIVIDUAL) {
                    // For individual chats, users can always leave
                    return true;
                }
                
                // For GROUP and CHANNEL, only ADMIN and OWNER can delete the entire chat
                Role role = participant.getRole();
                if (role == Role.ADMIN || role == Role.OWNER) {
                    return true;
                }
                
                // Regular members can only leave the chat, not delete it completely
                return true;
            }
        }
        
        // User not found in participants
        logger.info("User {} is not a participant in chat {}", user.getId(), chat.getId());
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChatTypeEditable(ChatType chatType) {
        return chatType != ChatType.INDIVIDUAL;
    }
} 