package com.ap4.client.interfaces.services;

import com.ap4.common.enums.ChatType;
import com.ap4.common.models.Chat;
import com.ap4.common.models.User;

/**
 * Interface for authorization service that handles permission checks based on user roles.
 * This service is responsible for determining if users have permission to perform
 * specific actions based on their roles and the context of the action.
 */
public interface IAuthorizationService {
    
    /**
     * Checks if a user can edit a chat.
     * 
     * @param chat The chat to be edited
     * @param user The user attempting to edit
     * @return true if the user has permission to edit the chat, false otherwise
     */
    boolean canEditChat(Chat chat, User user);
    
    /**
     * Checks if a user can delete or leave a chat.
     * 
     * @param chat The chat to be deleted or left
     * @param user The user attempting to delete/leave
     * @return true if the user has permission to delete/leave the chat, false otherwise
     */
    boolean canDeleteChat(Chat chat, User user);
    
    /**
     * Checks if a chat type can be edited.
     * Some chat types like INDIVIDUAL may not be editable regardless of user roles.
     * 
     * @param chatType The type of chat
     * @return true if the chat type is editable, false otherwise
     */
    boolean isChatTypeEditable(ChatType chatType);
} 