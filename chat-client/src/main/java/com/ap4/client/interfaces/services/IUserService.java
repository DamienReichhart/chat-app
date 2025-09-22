package com.ap4.client.interfaces.services;

import com.ap4.common.models.User;
import java.util.List;

/**
 * Interface for user-related operations.
 * <p>
 * This interface defines methods for user authentication, registration, and management.
 * </p>
 */
public interface IUserService {

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username the username of the user
     * @param password the password of the user (plain text)
     * @return the user if authentication is successful; {@code null} otherwise
     */
    User authenticateUser(String username, String password);

    /**
     * Registers a new user.
     *
     * @param username the username for the new user
     * @param email    the email for the new user
     * @param password the password for the new user (plain text)
     * @return the newly created user if registration is successful; {@code null} if validation fails
     */
    User registerUser(String username, String email, String password);

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the user if found; {@code null} otherwise
     */
    User getUserById(int userId);

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to retrieve
     * @return the user if found; {@code null} otherwise
     */
    User getUserByUsername(String username);

    /**
     * Retrieves a user by their email.
     *
     * @param email the email of the user to retrieve
     * @return the user if found; {@code null} otherwise
     */
    User getUserByEmail(String email);

    /**
     * Updates an existing user.
     *
     * @param user the user to update
     */
    void updateUser(User user);

    /**
     * Updates a user's password.
     *
     * @param userId      the ID of the user
     * @param newPassword the new password (plain text)
     * @return {@code true} if the password was updated successfully; {@code false} otherwise
     */
    boolean updatePassword(int userId, String newPassword);

    /**
     * Searches for users matching the given query.
     *
     * @param query the search query
     * @return a list of users matching the query
     */
    List<User> searchUsers(String query);
}
