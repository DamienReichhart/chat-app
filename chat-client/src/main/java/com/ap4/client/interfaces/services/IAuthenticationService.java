package com.ap4.client.interfaces.services;

import com.ap4.common.models.User;

/**
 * Service interface for authentication-related operations.
 * Handles user login, registration, and session management.
 */
public interface IAuthenticationService {
    
    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username of the user
     * @param password The password of the user (plain text)
     * @return The authenticated user if successful, null otherwise
     * @throws IllegalArgumentException if the credentials are invalid
     */
    User login(String username, String password);
    
    /**
     * Registers a new user with the provided information.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user (plain text)
     * @param confirmPassword The password confirmation (for validation)
     * @return The newly registered user if successful
     * @throws IllegalArgumentException if the registration fails due to validation issues
     */
    User register(String username, String email, String password, String confirmPassword);
    
    /**
     * Validates the login credentials without performing the actual login.
     *
     * @param username The username to validate
     * @param password The password to validate
     * @return true if the credentials are valid, false otherwise
     */
    boolean validateLoginCredentials(String username, String password);
    
    /**
     * Validates registration data without performing the actual registration.
     *
     * @param username The username to validate
     * @param email The email to validate
     * @param password The password to validate
     * @param confirmPassword The password confirmation to validate
     * @return true if the registration data is valid, false otherwise
     * @throws IllegalArgumentException with a specific message if validation fails
     */
    boolean validateRegistrationData(String username, String email, String password, String confirmPassword);
    
    /**
     * Logs out the current user.
     */
    void logout();
} 