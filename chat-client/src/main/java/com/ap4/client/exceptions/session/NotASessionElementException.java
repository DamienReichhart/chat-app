package com.ap4.client.exceptions.session;

/**
 * Exception thrown when a required element is not found in the session.
 */
public class NotASessionElementException extends RuntimeException {
    
    public NotASessionElementException(String message) {
        super(message);
    }
    
    /**
     * Constructor with element name that creates a formatted error message.
     * 
     * @param name The name of the missing session element
     */
    public NotASessionElementException(String name, boolean isElementName) {
        super("Required session element '" + name + "' not found");
    }
} 