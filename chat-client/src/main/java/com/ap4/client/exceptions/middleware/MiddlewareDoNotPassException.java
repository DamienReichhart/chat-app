package com.ap4.client.exceptions.middleware;

/**
 * Exception thrown by middlewares to interrupt the middleware chain processing.
 * Used when a middleware determines that further processing should be stopped,
 * typically due to validation failures, authentication issues, or other conditions
 * that prevent normal execution flow.
 * 
 * This exception is part of the middleware infrastructure that implements
 * the Chain of Responsibility pattern in the application.
 */
public class MiddlewareDoNotPassException extends Exception {
    
    /**
     * Creates a new MiddlewareDoNotPassException with information about which
     * middleware interrupted the processing chain.
     * 
     * @param middlewareName The name of the middleware that threw the exception
     */
    public MiddlewareDoNotPassException(String middlewareName) {
        super("The middleware " + middlewareName + " do not pass: ");
    }
}
