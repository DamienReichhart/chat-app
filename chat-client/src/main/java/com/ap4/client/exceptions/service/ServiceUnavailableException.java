package com.ap4.client.exceptions.service;

/**
 * Exception thrown when a required service is unavailable.
 */
public class ServiceUnavailableException extends RuntimeException {
    
    private final String serviceName;
    
    public ServiceUnavailableException(String message) {
        super(message);
        this.serviceName = null;
    }
    
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = null;
    }
    
    /**
     * Creates a new exception with a specified service name and reason.
     * 
     * @param serviceName The name of the unavailable service
     * @param reason The reason why the service is unavailable
     * @param isService Flag to distinguish from the other constructor (not used)
     */
    public ServiceUnavailableException(String serviceName, String reason, boolean isService) {
        super("Service '" + serviceName + "' is unavailable: " + reason);
        this.serviceName = serviceName;
    }
    
    /**
     * Creates a new exception with a specified service name and underlying cause.
     * 
     * @param serviceName The name of the unavailable service
     * @param cause The underlying cause of the service unavailability
     * @param isService Flag to distinguish from the other constructor (not used)
     */
    public ServiceUnavailableException(String serviceName, Throwable cause, boolean isService) {
        super("Service '" + serviceName + "' is unavailable", cause);
        this.serviceName = serviceName;
    }
    
    /**
     * Creates a new exception with a specified service name, reason message, and cause.
     * 
     * @param serviceName The name of the unavailable service
     * @param reason The reason why the service is unavailable
     * @param cause The underlying cause of the service unavailability
     */
    public ServiceUnavailableException(String serviceName, String reason, Throwable cause) {
        super("Service '" + serviceName + "' is unavailable: " + reason, cause);
        this.serviceName = serviceName;
    }
    
    /**
     * Gets the name of the service that is unavailable.
     * 
     * @return The service name, or null if not specified
     */
    public String getServiceName() {
        return serviceName;
    }
} 