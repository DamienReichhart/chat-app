package com.ap4.client.exceptions.validation;

import com.ap4.client.exceptions.data.DataAccessException;

/**
 * Exception thrown when a requested user cannot be found.
 */
public class UserNotFoundException extends DataAccessException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(int userId) {
        super("User with ID " + userId + " not found");
    }
    
    public UserNotFoundException(String field, String value) {
        super("User with " + field + " '" + value + "' not found");
    }
} 