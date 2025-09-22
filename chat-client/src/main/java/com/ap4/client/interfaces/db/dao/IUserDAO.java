package com.ap4.client.interfaces.db.dao;

import com.ap4.common.models.User;

import java.util.List;

/**
 * Interface for User entity data access operations.
 * Defines specific methods for User entity beyond the standard CRUD operations.
 */
public interface IUserDAO extends IDAO<User, Integer> {
    
    /**
     * Retrieves a user by username.
     * 
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    User getByUsername(String username);
    
    /**
     * Retrieves a user by email address.
     * 
     * @param email The email address to search for
     * @return The user if found, null otherwise
     */
    User getByEmail(String email);
    
    /**
     * Retrieves all users in the system.
     * 
     * @return A list of all users
     */
    List<User> getAll();
    
    /**
     * Searches for users by username pattern.
     * 
     * @param usernamePattern The pattern to search for in usernames
     * @return A list of users matching the pattern
     */
    List<User> searchByUsername(String usernamePattern);
    
    /**
     * Checks if a username is already in use.
     * 
     * @param username The username to check
     * @return true if the username is in use, false otherwise
     */
    boolean isUsernameInUse(String username);
    
    /**
     * Checks if an email address is already in use.
     * 
     * @param email The email address to check
     * @return true if the email is in use, false otherwise
     */
    boolean isEmailInUse(String email);
}
