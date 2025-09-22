package com.ap4.client.interfaces.db.dao;

/**
 * Base DAO (Data Access Object) interface.
 * Defines the common contract for all DAO implementations.
 * 
 * @param <T> The entity type this DAO handles
 * @param <K> The primary key type of the entity
 */
public interface IDAO<T, K> {
    
    /**
     * Creates a new entity in the data store.
     * 
     * @param entity The entity to create
     * @return The created entity with any generated fields populated
     */
    T create(T entity);
    
    /**
     * Retrieves an entity by its primary key.
     * 
     * @param id The primary key of the entity to retrieve
     * @return The found entity or null if not found
     */
    T getById(K id);
    
    /**
     * Updates an existing entity in the data store.
     * 
     * @param entity The entity to update
     * @return The updated entity
     */
    T update(T entity);
    
    /**
     * Deletes an entity from the data store.
     * 
     * @param id The primary key of the entity to delete
     * @return true if the entity was deleted, false otherwise
     */
    boolean delete(K id);
    
    /**
     * Invalidates any cache entries for this DAO.
     */
    void invalidateCache();
}
