package com.ap4.client.interfaces.services;

import java.util.List;

import com.ap4.common.models.Chat;
import com.ap4.common.models.User;

/**
 * Service interface for contact-related operations.
 * Manages direct chat functionality and user contacts.
 */
public interface IContactService {
    
    /**
     * Creates a direct chat with another user.
     *
     * @param currentUser The current user
     * @param contactUser The user to chat with
     * @return The created direct chat
     * @throws IllegalArgumentException if the users are invalid
     */
    Chat createDirectChat(User currentUser, User contactUser);
    
    /**
     * Gets the direct chat between two users if it exists.
     *
     * @param currentUser The current user
     * @param contactUser The contact user
     * @return The direct chat if it exists, null otherwise
     */
    Chat getDirectChat(User currentUser, User contactUser);
    
    /**
     * Gets all direct chats for the current user.
     *
     * @param currentUser The current user
     * @return A list of direct chats
     */
    List<Chat> getDirectChats(User currentUser);
    
    /**
     * Gets users that can be added as contacts for the current user.
     * This excludes users the current user already has direct chats with.
     *
     * @param currentUser The current user
     * @return A list of potential contacts
     */
    List<User> getPotentialContacts(User currentUser);
    
    /**
     * Gets all users the current user has direct chats with.
     *
     * @param currentUser The current user
     * @return A list of contact users
     */
    List<User> getContacts(User currentUser);
    
    /**
     * Searches for potential contacts with a query string.
     *
     * @param query The search query
     * @param currentUser The current user
     * @return A list of users matching the query who are not already contacts
     */
    List<User> searchPotentialContacts(String query, User currentUser);
    
    /**
     * Removes a direct chat with a contact.
     * 
     * @param chatId The direct chat ID
     * @param currentUser The current user
     * @throws IllegalArgumentException if the chat is not a direct chat
     * @throws SecurityException if the user doesn't have permission
     */
    void removeDirectChat(int chatId, User currentUser);
    
    /**
     * Validates that a direct chat can be created between two users.
     *
     * @param currentUser The current user
     * @param contactUser The potential contact
     * @return true if the direct chat can be created
     * @throws IllegalArgumentException with specific message if validation fails
     */
    boolean validateDirectChatCreation(User currentUser, User contactUser);
} 