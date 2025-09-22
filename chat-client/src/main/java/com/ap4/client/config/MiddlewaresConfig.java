package com.ap4.client.config;

import com.ap4.client.services.SessionService;
import com.ap4.client.middlewares.Middleware;

import java.util.HashMap;

/**
 * Configuration class for managing middleware components in the application.
 * Allows registration and retrieval of middleware chains for different application paths.
 * 
 * Middlewares are used to implement cross-cutting concerns like authentication,
 * logging, and data validation across different areas of the application.
 * This class provides a centralized registry for middleware configurations.
 */
public class MiddlewaresConfig {
    /**
     * The application session instance.
     * Used to provide context to middleware components.
     */
    private SessionService sessionService;
    
    /**
     * HashMap storing middleware chains by name.
     * Each entry consists of a name (key) and an array of middleware components (value).
     */
    private HashMap<String, Middleware[]> config;

    /**
     * Creates a new MiddlewaresConfig instance with the provided session.
     * Initializes the middleware registry and registers the global middleware chain.
     * 
     * @param sessionService The application session instance
     */
    public MiddlewaresConfig(SessionService sessionService) {
        this.config = new HashMap<>();
        this.sessionService = sessionService;
        // Initialize with an empty global middleware chain
        this.registerMiddleware("global", new Middleware[] {});
    }

    /**
     * Registers a middleware chain with the specified name.
     * If a chain with the same name already exists, it will be overwritten.
     * 
     * @param name The name of the middleware chain
     * @param middlewares The array of middleware components in the chain
     */
    final private void registerMiddleware(String name, Middleware[] middlewares) {
        this.config.put(name, middlewares);
    }

    /**
     * Retrieves a middleware chain by name.
     * 
     * @param name The name of the middleware chain to retrieve
     * @return The array of middleware components, or null if the name is not registered
     */
    final public Middleware[] getMiddleware(String name) {
        return this.config.get(name);
    }
}
