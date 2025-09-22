package com.ap4.client.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.exceptions.db.QueryExecutionException;
import com.ap4.client.exceptions.validation.UserNotFoundException;
import com.ap4.client.interfaces.db.dao.IUserDAO;
import com.ap4.common.models.User;

public class UserDAO extends AbstractDAO<User, Integer> implements IUserDAO {

    public UserDAO() {
        super("users");
    }

    /**
     * Validates a user entity before database operations.
     * 
     * @param user The user to validate
     * @throws InvalidDataException if validation fails
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new InvalidDataException("User", "Entity cannot be null");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new InvalidDataException("User", "username", "cannot be empty");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new InvalidDataException("User", "password", "cannot be empty");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new InvalidDataException("User", "email", "cannot be empty");
        }
        
        // Email format validation could be added here
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidDataException("User", "email", "has invalid format");
        }
    }

    public User create(User user) {
        // Validate the user entity
        validateUser(user);
        
        // Check for duplicates before trying to insert
        if (fetchUserByUsername(user.getUsername()) != null) {
            throw new DuplicateKeyException("User", "username", user.getUsername());
        }
        
        if (fetchUserByEmail(user.getEmail()) != null) {
            throw new DuplicateKeyException("User", "email", user.getEmail());
        }
        
        String query = "INSERT INTO " + tableName + " (username, password, email) VALUES (?, ?, ?)";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        user.getUsername(), 
                        user.getPassword(), 
                        user.getEmail())) {
                    statement.executeUpdate();
                    
                    // Get the generated ID
                    try (var generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getInt(1));
                        }
                    } catch (SQLException e) {
                        logger.warn("Could not retrieve generated ID for new user", e);
                    }
                    
                    logger.trace("User created : " + user.toString());
                    return user;
                }
            });
        } catch (SQLException e) {
            logger.error("Failed to create user: " + e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            // Handle specific database-reported duplicate key errors with more precise messages
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                if (errorMessage.contains("username")) {
                    throw new DuplicateKeyException("User", "username", user.getUsername());
                } else if (errorMessage.contains("email")) {
                    throw new DuplicateKeyException("User", "email", user.getEmail());
                } else {
                    throw new DataCreationException("User creation failed: A user with the same unique identifier already exists");
                }
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to create user", e);
            } else {
                throw new DataCreationException("Failed to create user: " + e.getMessage(), e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error creating user: " + e.getMessage(), e);
            throw new DataCreationException("An unexpected error occurred while creating the user", e);
        }
    }

    public User update(User user) {
        // Validate the user entity
        validateUser(user);
        
        // Make sure the user exists
        User existingUser = fetchUserById(user.getId());
        if (existingUser == null) {
            throw new UserNotFoundException(user.getId());
        }
        
        // Check for duplicate username if it has changed
        if (!existingUser.getUsername().equals(user.getUsername()) &&
            fetchUserByUsername(user.getUsername()) != null) {
            throw new DuplicateKeyException("User", "username", user.getUsername());
        }
        
        // Check for duplicate email if it has changed
        if (!existingUser.getEmail().equals(user.getEmail()) &&
            fetchUserByEmail(user.getEmail()) != null) {
            throw new DuplicateKeyException("User", "email", user.getEmail());
        }
        
        String query = "UPDATE " + tableName + " SET username = ?, password = ?, email = ? WHERE id = ?";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        user.getUsername(), 
                        user.getPassword(), 
                        user.getEmail(), 
                        user.getId())) {
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new UserNotFoundException(user.getId());
                    }
                    logger.trace("User updated : " + user.toString());
                    return user;
                }
            });
        } catch (UserNotFoundException e) {
            // Rethrow user not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to update user: " + e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            // Handle specific database-reported duplicate key errors with more precise messages
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                if (errorMessage.contains("username")) {
                    throw new DuplicateKeyException("User", "username", user.getUsername());
                } else if (errorMessage.contains("email")) {
                    throw new DuplicateKeyException("User", "email", user.getEmail());
                } else {
                    throw new DataUpdateException("User update failed due to duplicate key constraint");
                }
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to update user", e);
            } else {
                throw new DataUpdateException("Failed to update user: " + e.getMessage(), e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error updating user: " + e.getMessage(), e);
            throw new DataUpdateException("An unexpected error occurred while updating the user", e);
        }
    }

    public User getById(int id) {
        if (id <= 0) {
            throw new InvalidDataException("User", "id", "must be a positive number");
        }
        
        User user = fetchUserById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }
    
    private User fetchUserById(int id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id);
                     ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        logger.trace("User getById : " + resultSet.getString("username"));
                        return new User(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"),
                                resultSet.getTimestamp("created_at")
                        );
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Failed to fetch user by ID: " + e.getMessage(), e);
            if (e.getMessage().contains("connect")) {
                throw new DatabaseConnectionException("fetch user by ID", e);
            } else {
                throw new QueryExecutionException("user fetch by ID", e.getMessage(), e);
            }
        }
    }

    public User getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidDataException("User", "username", "cannot be empty");
        }
        
        User user = fetchUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("username", username);
        }
        return user;
    }
    
    private User fetchUserByUsername(String username) {
        String query = "SELECT * FROM " + tableName + " WHERE username = ?";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, username);
                     ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        logger.trace("User getByUsername : " + resultSet.getString("username"));
                        return new User(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"),
                                resultSet.getTimestamp("created_at")
                        );
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Failed to fetch user by username: " + e.getMessage(), e);
            if (e.getMessage().contains("connect")) {
                throw new DatabaseConnectionException("fetch user by username", e);
            } else {
                throw new QueryExecutionException("user fetch by username", e.getMessage(), e);
            }
        }
    }

    public List<User> getAll() {
        return fetchAllUsers();
    }
    
    private List<User> fetchAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        users.add(new User(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"),
                                resultSet.getTimestamp("created_at")
                        ));
                    }
                    users.forEach(user -> logger.trace("User getAll : " + user.toString()));
                    return users;
                }
            });
        } catch (SQLException e) {  
            logger.error("Failed to fetch all users: " + e.getMessage(), e);
            return users;
        }
    }

    public User getByEmail(String email) {
        User user = fetchUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("email", email);
        }
        return user;
    }
    
    private User fetchUserByEmail(String email) {
        String query = "SELECT * FROM " + tableName + " WHERE email = ?";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, email);
                     ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        logger.trace("User getByEmail : " + resultSet.getString("email"));
                        return new User(
                                resultSet.getInt("id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"),
                                resultSet.getTimestamp("created_at")
                        );
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.trace("User getByEmail(" + email + ") : null");
            return null;
        }
    }

    @Override
    public User getById(Integer id) {
        return getById(id.intValue());
    }

    @Override
    public boolean delete(Integer id) {
        if (id <= 0) {
            throw new InvalidDataException("User", "id", "must be a positive number");
        }
        
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id)) {
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new UserNotFoundException(id);
                    }
                    return true;
                }
            });
        } catch (UserNotFoundException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to delete user: " + e.getMessage(), e);
            if (e.getMessage().contains("foreign key constraint")) {
                throw new EntityRelationshipException("User", id, "related entity", 0, "delete");
            } else if (e.getMessage().contains("connect")) {
                throw new DatabaseConnectionException("delete user", e);
            } else {
                throw new DataDeletionException("User", id, e);
            }
        }
    }

    @Override
    public List<User> searchByUsername(String usernamePattern) {
        return fetchAllUsers().stream()
                .filter(user -> user.getUsername().matches(".*" + usernamePattern + ".*"))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUsernameInUse(String username) {
        try {
            fetchUserByUsername(username);
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isEmailInUse(String email) {
        try {
            fetchUserByEmail(email);
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }
    
    // Helper methods to check existence without throwing exceptions - used by services
    public boolean userExistsByUsername(String username) {
        return fetchUserByUsername(username) != null;
    }
    
    public boolean userExistsByEmail(String email) {
        return fetchUserByEmail(email) != null;
    }
}
