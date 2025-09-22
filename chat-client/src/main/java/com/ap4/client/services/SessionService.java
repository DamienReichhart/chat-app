package com.ap4.client.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.exceptions.session.NotASessionElementException;
import com.ap4.common.models.User;

/**
 * Session service for managing application session state.
 * This is a singleton service that provides access to session data
 * throughout the application lifecycle.
 */
public class SessionService {
    private static final Logger logger = LogManager.getLogger(SessionService.class);
    private static SessionService instance;
    
    // Session storage
    private final Map<String, Object> sessionData;
    
    /**
     * The currently logged-in user.
     */
    private User currentUser;
    
    /**
     * Authentication token for the current session.
     */
    private String authToken;
    
    /**
     * Session start time.
     */
    private LocalDateTime sessionStartTime;
    
    /**
     * Last activity time.
     */
    private LocalDateTime lastActivityTime;

    /**
     * Private constructor for singleton pattern.
     * Initializes session storage.
     */
    private SessionService() {
        this.sessionData = new HashMap<>();
        this.sessionStartTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        logger.debug("SessionService initialized");
    }
    
    /**
     * Get the singleton instance of the SessionService.
     * 
     * @return The SessionService instance
     */
    public static synchronized SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }
    
    /**
     * Set a value in the session with a specific key.
     * 
     * @param <T> The type of the value
     * @param key The key to store the value under
     * @param value The value to store
     * @throws IllegalArgumentException if key is null or empty
     */
    public <T> void set(String key, T value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Session key cannot be null or empty");
        }
        
        logger.debug("Setting session data - key: {}, value type: {}", key, value != null ? value.getClass().getSimpleName() : "null");
        sessionData.put(key, value);
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Get a value from the session with a specific key.
     * 
     * @param <T> The expected type of the value
     * @param key The key to retrieve the value for
     * @param type The class of the expected value type
     * @return The value
     * @throws NotASessionElementException if the value does not exist or has wrong type
     */
    public <T> T get(String key, Class<T> type) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Session key cannot be null or empty");
        }
        
        Object value = sessionData.get(key);
        
        if (value == null) {
            logger.debug("Session data not found for key: {}", key);
            throw new NotASessionElementException(key);
        }
        
        if (type.isInstance(value)) {
            logger.debug("Retrieved session data - key: {}, value type: {}", key, value.getClass().getSimpleName());
            return type.cast(value);
        } else {
            logger.warn("Type mismatch for session data - key: {}, expected: {}, actual: {}", 
                    key, type.getSimpleName(), value.getClass().getSimpleName());
            throw new NotASessionElementException("Session element '" + key + "' has incorrect type. Expected " + 
                                                 type.getSimpleName() + " but found " + value.getClass().getSimpleName());
        }
    }
    
    /**
     * Get a string value from the session.
     * Convenience method for getting string values.
     * 
     * @param key The key to retrieve the value for
     * @return The string value
     * @throws NotASessionElementException if the value does not exist or is not a string
     */
    public String getString(String key) {
        return get(key, String.class);
    }
    
    /**
     * Get an integer value from the session.
     * Convenience method for getting integer values.
     * 
     * @param key The key to retrieve the value for
     * @return The integer value
     * @throws NotASessionElementException if the value does not exist or is not an integer
     */
    public Integer getInteger(String key) {
        return get(key, Integer.class);
    }
    
    /**
     * Get a boolean value from the session.
     * Convenience method for getting boolean values.
     * 
     * @param key The key to retrieve the value for
     * @return The boolean value
     * @throws NotASessionElementException if the value does not exist or is not a boolean
     */
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }
    
    /**
     * Get a value from the session as an Optional.
     * Useful when null checks are needed and exceptions should be avoided.
     * 
     * @param <T> The expected type of the value
     * @param key The key to retrieve the value for
     * @param type The class of the expected value type
     * @return An Optional containing the value, or empty if not found or wrong type
     */
    public <T> Optional<T> getOptional(String key, Class<T> type) {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            return Optional.ofNullable(get(key, type));
        } catch (NotASessionElementException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Check if a key exists in the session.
     * 
     * @param key The key to check
     * @return True if the key exists, false otherwise
     */
    public boolean has(String key) {
        boolean exists = sessionData.containsKey(key);
        logger.debug("Checking session data existence - key: {}, exists: {}", key, exists);
        return exists;
    }
    
    /**
     * Remove a value from the session.
     * 
     * @param key The key to remove
     * @return The removed value, or null if the key didn't exist
     */
    public Object remove(String key) {
        logger.debug("Removing session data - key: {}", key);
        return sessionData.remove(key);
    }
    
    /**
     * Clear all session data.
     */
    public void clear() {
        logger.debug("Clearing all session data");
        sessionData.clear();
        this.currentUser = null;
        this.authToken = null;
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Get the number of items in the session.
     * 
     * @return The number of key-value pairs in the session
     */
    public int size() {
        return sessionData.size();
    }

    /**
     * Gets a string value from the session (backward compatibility method).
     * 
     * @param key The key to retrieve
     * @return The string value associated with the key
     * @deprecated Use getString() instead
     */
    @Deprecated
    public String getElement(String key) {
        return getString(key);
    }
    
    /**
     * Sets a string value in the session (backward compatibility method).
     * 
     * @param key The key to set
     * @param value The value to store
     * @deprecated Use set() instead
     */
    @Deprecated
    public void setElement(String key, String value) {
        set(key, value);
    }
    
    /**
     * Checks if a key exists in the session (backward compatibility method).
     * 
     * @param key The key to check
     * @return True if the key exists, false otherwise
     * @deprecated Use has() instead
     */
    @Deprecated
    public boolean hasElement(String key) {
        return has(key);
    }
    
    /**
     * Sets the current user for the session.
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            logger.info("User set in session: {} (ID: {})", user.getUsername(), user.getId());
        } else {
            logger.info("Current user cleared from session");
        }
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Gets the current user from the session.
     * 
     * @return The current user
     * @throws NotASessionElementException if no user is logged in
     */
    public User getCurrentUser() {
        if (currentUser == null) {
            throw new NotASessionElementException("currentUser");
        }
        return currentUser;
    }
    
    /**
     * Sets the authentication token for the session.
     * 
     * @param token The authentication token
     */
    public void setAuthToken(String token) {
        this.authToken = token;
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Gets the current authentication token.
     * 
     * @return The authentication token
     * @throws NotASessionElementException if no auth token is set
     */
    public String getAuthToken() {
        if (authToken == null) {
            throw new NotASessionElementException("authToken");
        }
        return authToken;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return this.currentUser != null && this.authToken != null;
    }
    
    /**
     * Gets the session start time.
     * 
     * @return The session start time
     */
    public LocalDateTime getSessionStartTime() {
        return this.sessionStartTime;
    }
    
    /**
     * Gets the time of the last activity in the session.
     * 
     * @return The last activity time
     */
    public LocalDateTime getLastActivityTime() {
        return this.lastActivityTime;
    }
    
    /**
     * Updates the last activity time to the current time.
     */
    public void updateLastActivityTime() {
        this.lastActivityTime = LocalDateTime.now();
    }
}
