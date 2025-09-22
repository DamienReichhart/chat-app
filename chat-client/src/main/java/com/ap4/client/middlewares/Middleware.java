package com.ap4.client.middlewares;

import com.ap4.client.services.SessionService;
import com.ap4.client.exceptions.middleware.MiddlewareDoNotPassException;
import com.ap4.client.scenes.SuperScene;

/**
 * Abstract base class for all middleware components in the application.
 * 
 * Middleware components implement cross-cutting concerns like authentication,
 * authorization, validation, and logging that need to be applied across
 * multiple scenes or execution paths in the application.
 * 
 * This class follows the Chain of Responsibility pattern, where each middleware
 * can process a request and decide whether to pass it to the next handler in the chain.
 */
public abstract class Middleware {
    /**
     * The application session instance.
     * Provides access to session data that middleware components may need for processing.
     */
    protected SessionService sessionService;

    /**
     * Creates a new middleware component with the specified session.
     * 
     * @param sessionService The application session instance
     */
    public Middleware(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Handles the middleware processing for a specific scene.
     * Implementations should perform their specific processing and either:
     * 1. Allow the request to continue by completing normally, or
     * 2. Interrupt processing by throwing a MiddlewareDoNotPassException
     * 
     * @param scene The scene being processed by this middleware
     * @throws MiddlewareDoNotPassException If the middleware determines that processing should stop
     */
    public abstract void handle(SuperScene scene) throws MiddlewareDoNotPassException;
}
