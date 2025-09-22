package com.ap4.client.exceptions.security;

/**
 * Base exception for security-related issues.
 * This is a custom security exception different from java.lang.SecurityException.
 */
public class AppSecurityException extends RuntimeException {
    
    private final String securityComponent;
    
    public AppSecurityException(String message) {
        super(message);
        this.securityComponent = null;
    }
    
    public AppSecurityException(String message, Throwable cause) {
        super(message, cause);
        this.securityComponent = null;
    }
    
    public AppSecurityException(String message, String securityComponent) {
        super(message + (securityComponent != null ? " (component: " + securityComponent + ")" : ""));
        this.securityComponent = securityComponent;
    }
    
    public AppSecurityException(String message, String securityComponent, Throwable cause) {
        super(message + (securityComponent != null ? " (component: " + securityComponent + ")" : ""), cause);
        this.securityComponent = securityComponent;
    }
    
    /**
     * Gets the security component that caused the exception.
     * 
     * @return The security component, or null if not specified
     */
    public String getSecurityComponent() {
        return securityComponent;
    }
} 