package com.ap4.client.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ap4.client.exceptions.data.ChatNotFoundException;
import com.ap4.client.exceptions.data.ChatParticipantNotFoundException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.exceptions.validation.UserNotFoundException;
import com.ap4.client.interfaces.db.dao.IChatParticipantDAO;
import com.ap4.client.interfaces.db.dao.IUserDAO;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

public class ChatParticipantDAO extends AbstractDAO<ChatParticipant, Integer> implements IChatParticipantDAO {
    private static final String PARTICIPANTS_TABLE = "chat_participants";
    
    // We need these DAOs to fetch related entities
    private final IUserDAO userDAO;
    private final ChatDAO chatDAO;

    public ChatParticipantDAO() {
        super(PARTICIPANTS_TABLE);
        this.userDAO = new UserDAO();
        this.chatDAO = new ChatDAO();
    }

    /**
     * Creates a new ChatParticipant record.
     */
    @Override
    public ChatParticipant create(ChatParticipant participant) {
        if (participant == null) {
            throw new InvalidDataException("ChatParticipant", "Entity cannot be null");
        }
        
        if (participant.getUser() == null || participant.getUser().getId() <= 0) {
            throw new InvalidDataException("ChatParticipant", "user", "must be a valid entity with positive ID");
        }
        
        if (participant.getChat() == null || participant.getChat().getId() <= 0) {
            throw new InvalidDataException("ChatParticipant", "chat", "must be a valid entity with positive ID");
        }
        
        if (participant.getRole() == null) {
            throw new InvalidDataException("ChatParticipant", "role", "cannot be null");
        }
        
        // Check if the user exists
        try {
            userDAO.getById(participant.getUser().getId());
        } catch (UserNotFoundException e) {
            throw new EntityRelationshipException("User with ID " + participant.getUser().getId() + " does not exist", e);
        }
        
        // Check if the chat exists
        try {
            chatDAO.getById(participant.getChat().getId());
        } catch (ChatNotFoundException e) {
            throw new EntityRelationshipException("Chat with ID " + participant.getChat().getId() + " does not exist", e);
        }
        
        // Check if participant already exists
        ChatParticipant existingParticipant = fetchParticipantByUserAndChatId(participant.getUser().getId(), participant.getChat().getId());
        if (existingParticipant != null) {
            throw new DuplicateKeyException("ChatParticipant", "user_id and chat_id", 
                    "User is already a participant in this chat");
        }
        
        final String query = "INSERT INTO " + tableName + " (role, user_id, chat_id) VALUES (?, ?, ?) RETURNING id";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        participant.getRole(), 
                        participant.getUser().getId(), 
                        participant.getChat().getId())) {
                    
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            participant.setId(rs.getInt("id"));
                        } else {
                            throw new DataCreationException("ChatParticipant creation failed: No ID returned");
                        }
                    }
                    
                    logger.trace("ChatParticipant created: {}", participant);
                    return participant;
                }
            });
        } catch (SQLException e) {
            logger.error("Error creating chat participant: {}", participant, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                throw new DuplicateKeyException("ChatParticipant", "user_id and chat_id", 
                        "User is already a participant in this chat");
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("user_id")) {
                throw new EntityRelationshipException("User with ID " + participant.getUser().getId() + " does not exist", e);
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("chat_id")) {
                throw new EntityRelationshipException("Chat with ID " + participant.getChat().getId() + " does not exist", e);
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to create chat participant", e);
            } else {
                throw new DataCreationException("ChatParticipant", e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error creating chat participant: {}", e.getMessage(), e);
            throw new DataCreationException("An unexpected error occurred while creating the chat participant", e);
        }
    }

    /**
     * Updates an existing ChatParticipant record.
     */
    @Override
    public ChatParticipant update(ChatParticipant participant) {
        if (participant == null) {
            throw new InvalidDataException("ChatParticipant", "Entity cannot be null");
        }
        
        if (participant.getId() <= 0) {
            throw new InvalidDataException("ChatParticipant", "id", "must be a positive number");
        }
        
        if (participant.getUser() == null || participant.getUser().getId() <= 0) {
            throw new InvalidDataException("ChatParticipant", "user", "must be a valid entity with positive ID");
        }
        
        if (participant.getChat() == null || participant.getChat().getId() <= 0) {
            throw new InvalidDataException("ChatParticipant", "chat", "must be a valid entity with positive ID");
        }
        
        if (participant.getRole() == null) {
            throw new InvalidDataException("ChatParticipant", "role", "cannot be null");
        }
        
        // Check if participant exists
        ChatParticipant existingParticipant = fetchParticipantById(participant.getId());
        if (existingParticipant == null) {
            throw new ChatParticipantNotFoundException(participant.getId());
        }
        
        // Check if the user exists
        try {
            userDAO.getById(participant.getUser().getId());
        } catch (UserNotFoundException e) {
            throw new EntityRelationshipException("User with ID " + participant.getUser().getId() + " does not exist", e);
        }
        
        // Check if the chat exists
        try {
            chatDAO.getById(participant.getChat().getId());
        } catch (ChatNotFoundException e) {
            throw new EntityRelationshipException("Chat with ID " + participant.getChat().getId() + " does not exist", e);
        }
        
        // Check for duplicate if the user_id or chat_id has changed
        if (existingParticipant.getUser().getId() != participant.getUser().getId() || 
            existingParticipant.getChat().getId() != participant.getChat().getId()) {
            
            ChatParticipant duplicate = fetchParticipantByUserAndChatId(
                    participant.getUser().getId(), participant.getChat().getId());
            
            if (duplicate != null && duplicate.getId() != participant.getId()) {
                throw new DuplicateKeyException("ChatParticipant", "user_id and chat_id", 
                        "User is already a participant in this chat");
            }
        }
        
        final String query = "UPDATE " + tableName + " SET role = ?, user_id = ?, chat_id = ? WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        participant.getRole(),
                        participant.getUser().getId(), 
                        participant.getChat().getId(), 
                        participant.getId())) {
                    
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new ChatParticipantNotFoundException(participant.getId());
                    }
                    
                    logger.trace("ChatParticipant updated: {}", participant);
                    return participant;
                }
            });
        } catch (ChatParticipantNotFoundException e) {
            // Rethrow participant not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error updating chat participant: {}", participant, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                throw new DuplicateKeyException("ChatParticipant", "user_id and chat_id", 
                        "User is already a participant in this chat");
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("user_id")) {
                throw new EntityRelationshipException("User with ID " + participant.getUser().getId() + " does not exist", e);
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("chat_id")) {
                throw new EntityRelationshipException("Chat with ID " + participant.getChat().getId() + " does not exist", e);
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to update chat participant", e);
            } else {
                throw new DataUpdateException("ChatParticipant", participant.getId(), e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error updating chat participant: {}", e.getMessage(), e);
            throw new DataUpdateException("ChatParticipant", participant.getId(), 
                    "An unexpected error occurred while updating the chat participant");
        }
    }

    /**
     * Deletes a ChatParticipant by its ID.
     */
    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("ChatParticipant", "id", "must be a positive number");
        }
        
        // Check if the participant exists
        ChatParticipant participant = fetchParticipantById(id);
        if (participant == null) {
            throw new ChatParticipantNotFoundException(id);
        }
        
        final String query = "DELETE FROM " + tableName + " WHERE id = ?";
        
        try {
            boolean result = executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        throw new ChatParticipantNotFoundException(id);
                    }
                    
                    logger.trace("ChatParticipant deleted, id: {}, rows affected: {}", id, rowsAffected);
                    return true;
                }
            });
            return result;
        } catch (ChatParticipantNotFoundException e) {
            // Rethrow participant not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error deleting chat participant with id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to delete chat participant", e);
            } else if (errorMessage.contains("foreign key") || errorMessage.contains("reference")) {
                throw new EntityRelationshipException("Cannot delete chat participant because it is referenced by other entities", e);
            } else {
                throw new DataDeletionException("ChatParticipant", id, e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error deleting chat participant: {}", e.getMessage(), e);
            throw new DataDeletionException("ChatParticipant", id, 
                    "An unexpected error occurred while deleting the chat participant");
        }
    }

    /**
     * Retrieves a ChatParticipant by its ID.
     */
    @Override
    public ChatParticipant getById(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("ChatParticipant", "id", "must be a positive number");
        }
        
        ChatParticipant participant = fetchParticipantById(id);
        if (participant == null) {
            throw new ChatParticipantNotFoundException(id);
        }
        return participant;
    }
    
    private ChatParticipant fetchParticipantById(Integer id) {
        final String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("ChatParticipant found by id: {}", id);
                        return mapResultSetToParticipant(rs);
                    }
                    
                    logger.trace("No ChatParticipant found for id: {}", id);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching chat participant by id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to fetch chat participant", e);
            } else {
                throw new DataAccessException("Failed to fetch chat participant with ID " + id, e);
            }
        } catch (Exception e) {
            logger.error("Unexpected error fetching chat participant by id: {}", id, e);
            throw new DataAccessException("An unexpected error occurred while fetching the chat participant", e);
        }
    }

    /**
     * Retrieves all ChatParticipants for a specific chat.
     */
    @Override
    public List<ChatParticipant> getByChatId(int chatId) {
        List<ChatParticipant> result = fetchParticipantsByChatId(chatId);
        return result;
    }
    
    private List<ChatParticipant> fetchParticipantsByChatId(int chatId) {
        final String query = "SELECT * FROM " + tableName + " WHERE chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                List<ChatParticipant> participants = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        participants.add(mapResultSetToParticipant(rs));
                    }
                }
                
                logger.trace("Fetched {} participants for chat id {}", participants.size(), chatId);
                return participants;
            });
        } catch (SQLException e) {
            logger.error("Error fetching participants by chat id: {}", chatId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all ChatParticipants for a specific user.
     */
    @Override
    public List<ChatParticipant> getByUserId(int userId) {
        return fetchParticipantsByUserId(userId);
    }
    
    private List<ChatParticipant> fetchParticipantsByUserId(int userId) {
        final String query = "SELECT * FROM " + tableName + " WHERE user_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                List<ChatParticipant> participants = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, userId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        participants.add(mapResultSetToParticipant(rs));
                    }
                }
                
                logger.trace("Fetched {} participants for user id {}", participants.size(), userId);
                return participants;
            });
        } catch (SQLException e) {
            logger.error("Error fetching participants by user id: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a ChatParticipant by user and chat IDs.
     * This method provides backward compatibility with code using getByUserIdAndChatId.
     */
    public ChatParticipant getByUserIdAndChatId(int userId, int chatId) {
        return getByUserAndChatId(userId, chatId);
    }

    /**
     * Retrieves a ChatParticipant by user and chat IDs.
     */
    @Override
    public ChatParticipant getByUserAndChatId(int userId, int chatId) {
        return fetchParticipantByUserAndChatId(userId, chatId);
    }
    
    private ChatParticipant fetchParticipantByUserAndChatId(int userId, int chatId) {
        final String query = "SELECT * FROM " + tableName + " WHERE user_id = ? AND chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, userId, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("ChatParticipant found for user {} in chat {}", userId, chatId);
                        return mapResultSetToParticipant(rs);
                    }
                    
                    logger.trace("No ChatParticipant found for user {} in chat {}", userId, chatId);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching chat participant for user {} in chat {}", userId, chatId, e);
            return null;
        }
    }

    /**
     * Deletes a ChatParticipant by its ID.
     * Equivalent to delete(id) but named differently for interface compatibility.
     */
    public boolean deleteById(int id) {
        return delete(id);
    }

    /**
     * Updates the role of a participant.
     */
    @Override
    public boolean updateRole(int userId, int chatId, Role newRole) {
        final String query = "UPDATE " + tableName + " SET role = ? WHERE user_id = ? AND chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, newRole, userId, chatId)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    logger.trace("Updated role for user {} in chat {} to {}, rows affected: {}", 
                            userId, chatId, newRole, rowsAffected);
                    return rowsAffected > 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error updating role for user {} in chat {} to {}", userId, chatId, newRole, e);
            return false;
        }
    }

    /**
     * Removes a participant from a chat.
     */
    @Override
    public boolean removeParticipant(int userId, int chatId) {
        final String query = "DELETE FROM " + tableName + " WHERE user_id = ? AND chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, userId, chatId)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    logger.trace("Removed user {} from chat {}, rows affected: {}", 
                            userId, chatId, rowsAffected);
                    return rowsAffected > 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error removing user {} from chat {}", userId, chatId, e);
            return false;
        }
    }

    /**
     * Checks if a user has a required role in a chat.
     */
    @Override
    public boolean hasPermission(int userId, int chatId, Role requiredRole) {
        ChatParticipant participant = getByUserAndChatId(userId, chatId);
        
        if (participant == null) {
            logger.trace("User {} does not have permission for role {} in chat {} (not a participant)", 
                    userId, requiredRole, chatId);
            return false;
        }
        
        boolean hasPermission = participant.getRole().ordinal() >= requiredRole.ordinal();
        logger.trace("User {} has role {} in chat {}, required role {}, hasPermission: {}", 
                userId, participant.getRole(), chatId, requiredRole, hasPermission);
        return hasPermission;
    }

    /**
     * Retrieves all participants with a specific role in a chat.
     */
    @Override
    public List<ChatParticipant> getByRole(int chatId, Role role) {
        return fetchParticipantsByRole(chatId, role);
    }
    
    private List<ChatParticipant> fetchParticipantsByRole(int chatId, Role role) {
        final String query = "SELECT * FROM " + tableName + " WHERE chat_id = ? AND role = ?";
        
        try {
            return executeWithConnection(connection -> {
                List<ChatParticipant> participants = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId, role);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        participants.add(mapResultSetToParticipant(rs));
                    }
                }
                
                logger.trace("Fetched {} participants with role {} in chat {}", 
                        participants.size(), role, chatId);
                return participants;
            });
        } catch (SQLException e) {
            logger.error("Error fetching participants with role {} in chat {}", role, chatId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Maps a ResultSet to a ChatParticipant object.
     */
    private ChatParticipant mapResultSetToParticipant(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int chatId = rs.getInt("chat_id");
        
        User user = userDAO.getById(userId);
        Chat chat = chatDAO.getById(chatId);
        
        if (user == null || chat == null) {
            logger.error("Failed to get user {} or chat {} for participant", userId, chatId);
            return null;
        }
        
        return new ChatParticipant(
            rs.getInt("id"),
            rs.getTimestamp("joined_at"),
            user,
            chat,
            Role.valueOf(rs.getString("role"))
        );
    }
}
