package com.ap4.client.exceptions.security;

/**
 * Exception thrown when a user does not have permission to perform an operation.
 */
public class AuthorizationException extends RuntimeException {
    
    private final String requiredPermission;
    private final String resource;
    
    public AuthorizationException(String message) {
        super(message);
        this.requiredPermission = null;
        this.resource = null;
    }
    
    public AuthorizationException(String message, String requiredPermission, String resource) {
        super(message);
        this.requiredPermission = requiredPermission;
        this.resource = resource;
    }
    
    public AuthorizationException(String requiredPermission, String resource) {
        super("Access denied: Missing permission '" + requiredPermission + "' for resource '" + resource + "'");
        this.requiredPermission = requiredPermission;
        this.resource = resource;
    }
    
    /**
     * Gets the permission that would be required to perform the operation.
     * 
     * @return The required permission, or null if not specified
     */
    public String getRequiredPermission() {
        return requiredPermission;
    }
    
    /**
     * Gets the resource that the user tried to access.
     * 
     * @return The resource, or null if not specified
     */
    public String getResource() {
        return resource;
    }
} 