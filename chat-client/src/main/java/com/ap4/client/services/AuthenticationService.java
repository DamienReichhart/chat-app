package com.ap4.client.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.exceptions.security.AuthenticationFailureException;
import com.ap4.client.exceptions.security.AuthorizationException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.validation.DataValidationException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.exceptions.service.ServiceUnavailableException;
import com.ap4.client.exceptions.validation.UserNotFoundException;
import com.ap4.client.interfaces.services.IAuthenticationService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.common.models.User;

/**
 * Implementation of the authentication service interface.
 * Handles user login, registration, and session management.
 */
public class AuthenticationService implements IAuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);
    
    private final IUserService userService;
    private final SessionService sessionService;
    private final WebSocketService webSocketService;
    
    /**
     * Default constructor for the authentication service.
     */
    public AuthenticationService() {
        this.userService = new UserService();
        this.sessionService = SessionService.getInstance();
        this.webSocketService = WebSocketService.getInstance();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public AuthenticationService(IUserService userService, SessionService sessionService, 
                               WebSocketService webSocketService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.webSocketService = webSocketService;
    }

    @Override
    public User login(String username, String password) {
        // Validate credentials
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login failed: Username is empty");
            throw new DataValidationException("Login failed")
                .addValidationError("username", "Username cannot be empty");
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Login failed: Password is empty");
            throw new DataValidationException("Login failed")
                .addValidationError("password", "Password cannot be empty");
        }
        
        try {
            // Reset any previous user state first
            webSocketService.reset();
            
            // Now authenticate the new user
            User user = userService.authenticateUser(username, password);
            
            // Store the user ID in the session
            sessionService.setElement("userId", String.valueOf(user.getId()));
            
            // Also store the User object directly in the session
            sessionService.setCurrentUser(user);
            
            // Generate and store an auth token
            String authToken = generateAuthToken(user);
            sessionService.setAuthToken(authToken);
            
            logger.info("User logged in: {} (ID: {})", username, user.getId());
            return user;
        } catch (UserNotFoundException e) {
            logger.warn("Login failed for user {}: User not found", username);
            throw AuthenticationFailureException.userNotFound(username);
        } catch (AuthenticationFailureException e) {
            logger.warn("Login failed for user {}: {}", username, e.getMessage());
            throw AuthenticationFailureException.invalidCredentials("Invalid username or password");
        } catch (ServiceUnavailableException e) {
            logger.error("Service unavailable during login for user {}: {}", username, e.getMessage());
            throw AuthenticationFailureException.connectionError("Authentication service is currently unavailable. Please try again later.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: {}", username, e.getMessage(), e);
            throw new AuthenticationFailureException("An unexpected error occurred during login", e);
        }
    }

    /**
     * Generates an authentication token for the user session.
     * This is a simple implementation for demonstration purposes.
     * 
     * @param user The authenticated user
     * @return A token string
     */
    private String generateAuthToken(User user) {
        // In a real application, this would generate a secure token
        // For this demonstration, we'll use a simple format
        return "AUTH_" + user.getId() + "_" + System.currentTimeMillis();
    }

    @Override
    public User register(String username, String email, String password, String confirmPassword) {
        try {
            if (!validateRegistrationData(username, email, password, confirmPassword)) {
                throw new DataValidationException("Registration validation failed");
            }
            
            User newUser = userService.registerUser(username, email, password);
            logger.info("User registered: {}", username);
            return newUser;
        } catch (DataValidationException e) {
            logger.warn("Registration validation failed for user {}: {}", username, e.getMessage());
            throw e;
        } catch (DuplicateKeyException e) {
            logger.warn("Registration failed for user {}: {}", username, e.getMessage());
            String field = e.getMessage().contains("username") ? "username" : "email";
            throw new DataValidationException("Registration failed")
                .addValidationError(field, e.getMessage());
        } catch (InvalidDataException e) {
            logger.warn("Registration failed for user {}: {}", username, e.getMessage());
            throw new DataValidationException("User validation failed")
                .addValidationError("input", e.getMessage());
        } catch (DataCreationException e) {
            logger.warn("Registration failed for user {}: {}", username, e.getMessage());
            throw new DataValidationException("Registration failed")
                .addValidationError("general", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during registration for user {}: {}", username, e.getMessage());
            throw new DataValidationException("Registration failed. Please try again.");
        }
    }

    @Override
    public boolean validateLoginCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            logger.debug("Login validation failed: username is empty");
            return false;
        }
        
        if (password == null || password.isEmpty()) {
            logger.debug("Login validation failed: password is empty");
            return false;
        }
        
        return true;
    }

    @Override
    public boolean validateRegistrationData(String username, String email, String password, String confirmPassword) {
        DataValidationException validationException = new DataValidationException("Registration validation failed");
        boolean isValid = true;
        
        if (username == null || username.trim().isEmpty()) {
            logger.debug("Registration validation failed: username is empty");
            validationException.addValidationError("username", "Username cannot be empty");
            isValid = false;
        }
        
        if (email == null || email.trim().isEmpty()) {
            logger.debug("Registration validation failed: email is empty");
            validationException.addValidationError("email", "Email cannot be empty");
            isValid = false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.debug("Registration validation failed: password is empty");
            validationException.addValidationError("password", "Password cannot be empty");
            isValid = false;
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            logger.debug("Registration validation failed: confirm password is empty");
            validationException.addValidationError("confirmPassword", "Confirm password cannot be empty");
            isValid = false;
        }
        
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            logger.debug("Registration validation failed: passwords do not match");
            validationException.addValidationError("confirmPassword", "Passwords do not match");
            isValid = false;
        }
        
        if (!isValid) {
            throw validationException;
        }
        
        return true;
    }

    @Override
    public void logout() {
        webSocketService.reset();
        sessionService.clear();
        logger.info("User logged out");
    }
    
    /**
     * Checks if the current user has permission to perform an action.
     * 
     * @param permission The permission required
     * @param resource The resource being accessed
     * @throws AuthorizationException if the user doesn't have permission
     */
    public void checkPermission(String permission, String resource) {
        User currentUser = sessionService.getCurrentUser();
        
        if (currentUser == null) {
            logger.warn("Permission check failed: No user logged in");
            throw new AuthorizationException(
                "You must be logged in to access this resource", 
                permission, 
                resource
            );
        }
        
        // For demonstration, we'll implement a simple permission system
        // This would be more sophisticated in a real application
        boolean hasPermission = hasPermission(currentUser, permission, resource);
        
        if (!hasPermission) {
            logger.warn("Permission denied: User {} does not have permission {} for resource {}", 
                     currentUser.getUsername(), permission, resource);
            throw new AuthorizationException(
                "You don't have permission to perform this action", 
                permission, 
                resource
            );
        }
    }

    /**
     * Determines if a user has a specific permission for a resource.
     * 
     * @param user The user to check
     * @param permission The permission required
     * @param resource The resource being accessed
     * @return true if the user has permission, false otherwise
     */
    private boolean hasPermission(User user, String permission, String resource) {
        // This is a placeholder implementation
        // In a real application, you'd check against a permission system
        
        // For this demo, we'll just return true (all authenticated users have all permissions)
        // Except for a few specific test cases
        if ("admin_action".equals(permission) && !user.getUsername().contains("admin")) {
            return false;
        }
        
        return true;
    }
} 