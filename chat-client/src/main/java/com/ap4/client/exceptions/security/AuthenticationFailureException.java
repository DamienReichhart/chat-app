package com.ap4.client.exceptions.security;

/**
 * Exception thrown when authentication operations fail.
 * This includes login failures, registration problems, and other authentication-related issues.
 */
public class AuthenticationFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Authentication failure reason types.
     */
    public enum FailureReason {
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        ACCOUNT_LOCKED,
        ACCOUNT_DISABLED,
        PASSWORD_EXPIRED,
        CONNECTION_ERROR,
        GENERAL_ERROR
    }
    
    private final FailureReason reason;
    
    /**
     * Creates a new AuthenticationFailureException with a message and failure reason.
     * 
     * @param message The error message
     * @param reason The reason for the authentication failure
     */
    public AuthenticationFailureException(String message, FailureReason reason) {
        super(message);
        this.reason = reason;
    }
    
    /**
     * Creates a new AuthenticationFailureException with a message, cause, and failure reason.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param reason The reason for the authentication failure
     */
    public AuthenticationFailureException(String message, Throwable cause, FailureReason reason) {
        super(message, cause);
        this.reason = reason;
    }
    
    /**
     * Creates a new AuthenticationFailureException with a message.
     * Uses GENERAL_ERROR as the default failure reason.
     * 
     * @param message The error message
     */
    public AuthenticationFailureException(String message) {
        super(message);
        this.reason = FailureReason.GENERAL_ERROR;
    }
    
    /**
     * Creates a new AuthenticationFailureException with a message and cause.
     * Uses GENERAL_ERROR as the default failure reason.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public AuthenticationFailureException(String message, Throwable cause) {
        super(message, cause);
        this.reason = FailureReason.GENERAL_ERROR;
    }
    
    /**
     * Gets the reason for the authentication failure.
     * 
     * @return The failure reason
     */
    public FailureReason getReason() {
        return reason;
    }
    
    /**
     * Creates an exception for invalid credentials.
     * 
     * @param message The error message
     * @return A new AuthenticationFailureException
     */
    public static AuthenticationFailureException invalidCredentials(String message) {
        return new AuthenticationFailureException(
            message != null ? message : "Invalid username or password", 
            FailureReason.INVALID_CREDENTIALS
        );
    }
    
    /**
     * Creates an exception for a user that already exists.
     * 
     * @param username The username that already exists
     * @return A new AuthenticationFailureException
     */
    public static AuthenticationFailureException userAlreadyExists(String username) {
        return new AuthenticationFailureException(
            "User '" + username + "' already exists", 
            FailureReason.USER_ALREADY_EXISTS
        );
    }
    
    /**
     * Creates an exception for a user that was not found.
     * 
     * @param username The username that was not found
     * @return A new AuthenticationFailureException
     */
    public static AuthenticationFailureException userNotFound(String username) {
        return new AuthenticationFailureException(
            "User '" + username + "' not found", 
            FailureReason.USER_NOT_FOUND
        );
    }
    
    /**
     * Creates an exception for connection errors.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @return A new AuthenticationFailureException
     */
    public static AuthenticationFailureException connectionError(String message, Throwable cause) {
        return new AuthenticationFailureException(
            message != null ? message : "Could not connect to authentication service", 
            cause,
            FailureReason.CONNECTION_ERROR
        );
    }
} 