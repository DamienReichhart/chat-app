package com.ap4.client.services;

import com.ap4.client.db.dao.UserDAO;
import com.ap4.client.exceptions.security.AuthenticationFailureException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.exceptions.db.QueryExecutionException;
import com.ap4.client.exceptions.validation.UserNotFoundException;
import com.ap4.client.interfaces.db.dao.IUserDAO;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.common.exceptions.EmptyHashException;
import com.ap4.common.exceptions.EmptyPasswordException;
import com.ap4.common.models.User;
import com.ap4.common.utils.PasswordUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service class for user-related operations.
 * Handles user authentication, registration, and management.
 */
public class UserService implements IUserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final IUserDAO userDAO;

    /**
     * Constructor with dependency injection for the DAO.
     */
    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Validates input for user-related operations.
     * This adds another layer of validation on top of the DAO-level validation.
     * 
     * @param username The username to validate
     * @param email The email to validate
     * @param password The password to validate
     * @throws DataValidationException if validation fails
     */
    private void validateUserInput(String username, String email, String password) {
        DataValidationException validationException = null;
        
        // Check if all fields are provided
        if (username == null || username.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            validationException = new DataValidationException("User validation failed");
            
            if (username == null || username.trim().isEmpty()) {
                validationException.addValidationError("username", "Username is required");
            }
            
            if (email == null || email.trim().isEmpty()) {
                validationException.addValidationError("email", "Email is required");
            }
            
            if (password == null || password.trim().isEmpty()) {
                validationException.addValidationError("password", "Password is required");
            }
            
            throw validationException;
        }
        
        // Additional validations
        validationException = new DataValidationException("User validation failed");
        
        // Username validations
        if (username.length() < 3) {
            validationException.addValidationError("username", "Username must be at least 3 characters long");
        }
        
        if (username.length() > 30) {
            validationException.addValidationError("username", "Username cannot exceed 30 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            validationException.addValidationError("username", "Username can only contain letters, numbers, and underscores");
        }
        
        // Email validations
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            validationException.addValidationError("email", "Invalid email format");
        }
        
        // Password validations
        if (password.length() < 6) {
            validationException.addValidationError("password", "Password must be at least 6 characters long");
        }
        
        if (validationException.getValidationErrors().size() > 0) {
            throw validationException;
        }
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username of the user
     * @param password The password of the user (plain text)
     * @return The authenticated user
     * @throws AuthenticationFailureException if authentication fails
     */
    public User authenticateUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Authentication failed: Empty credentials");
            throw AuthenticationFailureException.invalidCredentials("Username and password cannot be empty");
        }
        
        try {
            User user = userDAO.getByUsername(username);
            if (PasswordUtil.checkPassword(password, user.getPassword())) {
                logger.info("User '{}' authenticated successfully", username);
                return user;
            } else {
                logger.warn("Authentication failed: Invalid password for user '{}'", username);
                throw AuthenticationFailureException.invalidCredentials("Invalid username or password");
            }
        } catch (UserNotFoundException e) {
            logger.info("Authentication failed: User '{}' not found", username);
            throw AuthenticationFailureException.userNotFound(username);
        } catch (InvalidDataException e) {
            logger.warn("Authentication failed: Invalid input data - {}", e.getMessage());
            throw new AuthenticationFailureException(
                "Invalid input: " + e.getMessage(),
                AuthenticationFailureException.FailureReason.GENERAL_ERROR
            );
        } catch (DatabaseConnectionException e) {
            logger.error("Authentication failed: Database connection error - {}", e.getMessage(), e);
            throw AuthenticationFailureException.connectionError(
                "Unable to connect to the authentication service. Please try again later.", e);
        } catch (QueryExecutionException e) {
            logger.error("Authentication failed: Database query error - {}", e.getMessage(), e);
            throw AuthenticationFailureException.connectionError(
                "Error processing authentication request. Please try again later.", e);
        } catch (DataAccessException e) {
            logger.error("Authentication failed: Data access error - {}", e.getMessage(), e);
            throw AuthenticationFailureException.connectionError(
                "Error accessing user data. Please try again later.", e);
        } catch (EmptyHashException e) {
            logger.error("Authentication failed: Empty hash error - {}", e.getMessage(), e);
            throw new AuthenticationFailureException("Invalid password hash", e);
        } catch (EmptyPasswordException e) {
            logger.error("Authentication failed: Empty password error - {}", e.getMessage(), e);
            throw new AuthenticationFailureException("Invalid password", e);
        } catch (Exception e) {
            logger.error("Authentication failed: Unexpected error - {}", e.getMessage(), e);
            throw AuthenticationFailureException.connectionError(
                "An unexpected error occurred during authentication. Please try again later.", e);
        }
    }

    /**
     * Registers a new user.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user (plain text)
     * @return The newly created user
     * @throws DataValidationException if input validation fails
     * @throws DuplicateKeyException if username or email already exists
     * @throws DataCreationException if user creation fails
     */
    public User registerUser(String username, String email, String password) {
        try {
            // Perform service-level validation
            validateUserInput(username, email, password);
            
            // Create and save the new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(PasswordUtil.hashPassword(password));
            
            return userDAO.create(newUser);
        } catch (DuplicateKeyException e) {
            // Rethrow duplicate key exceptions directly
            logger.warn("Registration failed: {}", e.getMessage());
            throw e;
        } catch (InvalidDataException e) {
            // Convert InvalidDataException to a more user-friendly format
            logger.warn("Registration failed: Invalid data - {}", e.getMessage());
            DataValidationException validationException = new DataValidationException("User validation failed");
            validationException.addValidationError("input", e.getMessage());
            throw validationException;
        } catch (DatabaseConnectionException e) {
            logger.error("Registration failed: Database connection error - {}", e.getMessage(), e);
            throw new DataCreationException("Unable to connect to the database. Please try again later.", e);
        } catch (QueryExecutionException e) {
            logger.error("Registration failed: Query execution error - {}", e.getMessage(), e);
            throw new DataCreationException("Error processing registration request. Please try again later.", e);
        } catch (DataAccessException e) {
            logger.error("Registration failed: Data access error - {}", e.getMessage(), e);
            throw new DataCreationException("Error accessing user data. Please try again later.", e);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Registration failed: Unexpected error - {}", e.getMessage(), e);
            throw new DataCreationException("An unexpected error occurred during registration. Please try again later.", e);
        }
    }

    /**
     * Gets a user by their ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user if found
     * @throws UserNotFoundException if the user doesn't exist
     * @throws InvalidDataException if the ID is invalid
     */
    public User getUserById(int userId) {
        return userDAO.getById(userId);
    }

    /**
     * Gets a user by their username.
     *
     * @param username The username of the user to retrieve
     * @return The user if found
     * @throws UserNotFoundException if the user doesn't exist
     * @throws InvalidDataException if the username is invalid
     */
    public User getUserByUsername(String username) {
        return userDAO.getByUsername(username);
    }

    /**
     * Gets a user by their email.
     *
     * @param email The email of the user to retrieve
     * @return The user if found
     * @throws UserNotFoundException if the user doesn't exist
     * @throws InvalidDataException if the email is invalid
     */
    public User getUserByEmail(String email) {
        return userDAO.getByEmail(email);
    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update
     * @throws UserNotFoundException if the user doesn't exist
     * @throws InvalidDataException if the user data is invalid
     * @throws DuplicateKeyException if the username or email is already in use
     * @throws DataUpdateException if the update fails
     */
    public void updateUser(User user) {
        if (user == null) {
            throw new InvalidDataException("User", "Entity cannot be null");
        }
        
        // Additional service-level validation could be added here
        
        userDAO.update(user);
    }

    /**
     * Updates a user's password.
     *
     * @param userId The ID of the user
     * @param newPassword The new password (plain text)
     * @return true if the password was updated successfully, false otherwise
     * @throws UserNotFoundException if the user doesn't exist
     * @throws InvalidDataException if the new password is invalid
     * @throws DataUpdateException if the update fails
     */
    public boolean updatePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new InvalidDataException("User", "password", "cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new InvalidDataException("User", "password", "must be at least 6 characters long");
        }
        
        try {
            User user = userDAO.getById(userId);
            user.setPassword(PasswordUtil.hashPassword(newPassword)); 
            userDAO.update(user);
            return true;
        } catch (UserNotFoundException | DataUpdateException | InvalidDataException e) {
            logger.error("Failed to update password for user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating password for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Searches for users by a query string.
     *
     * @param query The search query
     * @return List of users matching the query
     */
    public java.util.List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        String lowercaseQuery = query.toLowerCase();
        return userDAO.getAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowercaseQuery) || 
                               (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowercaseQuery)))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Checks if a username is available (not already in use).
     * 
     * @param username The username to check
     * @return true if the username is available, false if it's already in use
     * @throws InvalidDataException if the username is invalid
     */
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidDataException("User", "username", "cannot be empty");
        }
        
        try {
            userDAO.getByUsername(username);
            // If we get here, the user was found, so the username is not available
            return false;
        } catch (UserNotFoundException e) {
            // Username not found, so it's available
            return true;
        }
    }
    
    /**
     * Checks if an email is available (not already in use).
     * 
     * @param email The email to check
     * @return true if the email is available, false if it's already in use
     * @throws InvalidDataException if the email is invalid
     */
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("User", "email", "cannot be empty");
        }
        
        try {
            userDAO.getByEmail(email);
            // If we get here, the user was found, so the email is not available
            return false;
        } catch (UserNotFoundException e) {
            // Email not found, so it's available
            return true;
        }
    }
}
