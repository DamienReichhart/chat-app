package com.ap4.client.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ap4.client.exceptions.data.ChatNotFoundException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataDeletionException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.db.DuplicateKeyException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.interfaces.db.dao.IChatDAO;
import com.ap4.common.enums.ChatType;
import com.ap4.common.models.Chat;

public class ChatDAO extends AbstractDAO<Chat, Integer> implements IChatDAO {
    private static final String CHATS_TABLE = "chats";
    private static final String CHAT_PARTICIPANTS_TABLE = "chat_participants";

    public ChatDAO() {
        super(CHATS_TABLE);
    }

    /**
     * Creates a new Chat record in the database.
     */
    @Override
    public Chat create(Chat chat) {
        if (chat == null) {
            throw new InvalidDataException("Chat", "Entity cannot be null");
        }
        
        if (chat.getName() == null || chat.getName().trim().isEmpty()) {
            throw new InvalidDataException("Chat", "name", "cannot be empty");
        }
        
        if (chat.getChatType() == null) {
            throw new InvalidDataException("Chat", "chatType", "cannot be null");
        }
        
        // Check if chat with same name already exists
        Chat existingChat = getByName(chat.getName());
        if (existingChat != null) {
            throw new DuplicateKeyException("Chat", "name", chat.getName());
        }
        
        final String query = "INSERT INTO " + tableName + " (chat_type, name, description) VALUES (?, ?, ?) RETURNING id";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        chat.getChatType(), chat.getName(), chat.getDescription())) {
                    
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            chat.setId(rs.getInt("id"));
                        } else {
                            throw new DataCreationException("Chat creation failed: No ID returned");
                        }
                    }
                    
                    logger.trace("Chat created: {}", chat);
                    return chat;
                }
            });
        } catch (SQLException e) {
            logger.error("Error creating chat: {}", chat, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                throw new DuplicateKeyException("Chat", "name", chat.getName());
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to create chat", e);
            } else {
                throw new DataCreationException("Chat", e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error creating chat: {}", e.getMessage(), e);
            throw new DataCreationException("An unexpected error occurred while creating the chat", e);
        }
    }

    /**
     * Updates an existing Chat record.
     */
    @Override
    public Chat update(Chat chat) {
        if (chat == null) {
            throw new InvalidDataException("Chat", "Entity cannot be null");
        }
        
        if (chat.getId() <= 0) {
            throw new InvalidDataException("Chat", "id", "must be a positive number");
        }
        
        if (chat.getName() == null || chat.getName().trim().isEmpty()) {
            throw new InvalidDataException("Chat", "name", "cannot be empty");
        }
        
        if (chat.getChatType() == null) {
            throw new InvalidDataException("Chat", "chatType", "cannot be null");
        }
        
        // Make sure the chat exists
        Chat existingChat = fetchChatById(chat.getId());
        if (existingChat == null) {
            throw new ChatNotFoundException(chat.getId());
        }
        
        // Check for duplicate name if name has changed
        if (!existingChat.getName().equals(chat.getName())) {
            Chat chatWithSameName = getByName(chat.getName());
            if (chatWithSameName != null && chatWithSameName.getId() != chat.getId()) {
                throw new DuplicateKeyException("Chat", "name", chat.getName());
            }
        }
        
        final String query = "UPDATE " + tableName + " SET chat_type = ?, name = ?, description = ? WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, 
                        chat.getChatType(), chat.getName(), chat.getDescription(), chat.getId())) {
                    
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new ChatNotFoundException(chat.getId());
                    }
                    
                    logger.trace("Chat updated: {}", chat);
                    return chat;
                }
            });
        } catch (ChatNotFoundException e) {
            // Rethrow chat not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error updating chat: {}", chat, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("duplicate key") || errorMessage.contains("unique constraint")) {
                throw new DuplicateKeyException("Chat", "name", chat.getName());
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to update chat", e);
            } else {
                throw new DataUpdateException("Chat", chat.getId(), e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error updating chat: {}", e.getMessage(), e);
            throw new DataUpdateException("Chat", chat.getId(), "An unexpected error occurred while updating the chat");
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Chat", "id", "must be a positive number");
        }
        
        // Make sure the chat exists
        Chat chat = fetchChatById(id);
        if (chat == null) {
            throw new ChatNotFoundException(id);
        }
        
        final String query = "DELETE FROM " + tableName + " WHERE id = ?";
        
        try {
            boolean result = executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        throw new ChatNotFoundException(id);
                    }
                    
                    logger.trace("Chat deleted, id: {}, rows affected: {}", id, rowsAffected);
                    return true;
                }
            });
            return result;
        } catch (ChatNotFoundException e) {
            // Rethrow chat not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error deleting chat with id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to delete chat", e);
            } else if (errorMessage.contains("foreign key") || errorMessage.contains("reference")) {
                throw new EntityRelationshipException("Cannot delete chat because it is referenced by other entities", e);
            } else {
                throw new DataDeletionException("Chat", id, e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error deleting chat: {}", e.getMessage(), e);
            throw new DataDeletionException("Chat", id, "An unexpected error occurred while deleting the chat");
        }
    }

    /**
     * Retrieves a Chat by its id.
     */
    @Override
    public Chat getById(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Chat", "id", "must be a positive number");
        }
        
        Chat chat = fetchChatById(id);
        if (chat == null) {
            throw new ChatNotFoundException(id);
        }
        return chat;
    }
    
    /**
     * Asynchronously retrieves a Chat by its id.
     * 
     * @param id The ID of the chat to retrieve
     * @return CompletableFuture that resolves to the chat or null if not found
     */
    public CompletableFuture<Chat> getByIdAsync(Integer id) {
        if (id == null || id <= 0) {
            CompletableFuture<Chat> future = new CompletableFuture<>();
            future.completeExceptionally(new InvalidDataException("Chat", "id", "must be a positive number"));
            return future;
        }
        
        // Execute async database operation
        return executeWithConnectionAsync(connection -> {
            final String query = "SELECT * FROM " + tableName + " WHERE id = ?";
            
            try (PreparedStatement statement = prepare(connection, query, id);
                 ResultSet rs = statement.executeQuery()) {
                
                if (rs.next()) {
                    Chat chat = mapResultSetToChat(rs);
                    logger.trace("Async chat found by id: {}", id);
                    return chat;
                }
                
                logger.trace("No chat found for id in async fetch: {}", id);
                return null;
            }
        }).thenApply(chat -> {
            if (chat == null) {
                throw new ChatNotFoundException(id);
            }
            return chat;
        });
    }
    
    private Chat fetchChatById(int id) {
        final String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("Chat found by id: {}", id);
                        return mapResultSetToChat(rs);
                    }
                    
                    logger.trace("No chat found for id: {}", id);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching chat by id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to fetch chat", e);
            } else {
                throw new DataAccessException("Failed to fetch chat with ID " + id, e);
            }
        } catch (Exception e) {
            logger.error("Unexpected error fetching chat by id: {}", id, e);
            throw new DataAccessException("An unexpected error occurred while fetching the chat", e);
        }
    }

    /**
     * Retrieves all Chat records from the database.
     */
    @Override
    public List<Chat> getAll() {
        return fetchAllChats();
    }
    
    private List<Chat> fetchAllChats() {
        final String query = "SELECT * FROM " + tableName;
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats", chats.size());
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error fetching all chats", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Chat getByName(String chatName) {
        return fetchChatByName(chatName);
    }
    
    private Chat fetchChatByName(String chatName) {
        final String query = "SELECT * FROM " + tableName + " WHERE name = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatName);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("Chat found by name: {}", chatName);
                        return mapResultSetToChat(rs);
                    }
                    
                    logger.trace("No chat found for name: {}", chatName);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching chat by name: {}", chatName, e);
            return null;
        }
    }

    @Override
    public int countByChatName(String chatName) {
        return fetchCountByChatName(chatName);
    }
    
    private int fetchCountByChatName(String chatName) {
        final String query = "SELECT COUNT(*) FROM " + tableName + " WHERE name = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatName);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    
                    return 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error counting chats by name: {}", chatName, e);
            return 0;
        }
    }

    @Override
    public boolean deleteByChatName(String chatName) {
        // Get the chat first for cache invalidation
        Chat chat = getByName(chatName);
        if (chat == null) {
            return false;
        }
        
        final String query = "DELETE FROM " + tableName + " WHERE name = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatName)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    logger.trace("Chat deleted by name: {}, rows affected: {}", chatName, rowsAffected);
                    return rowsAffected > 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error deleting chat by name: {}", chatName, e);
            return false;
        }
    }

    @Override
    public List<Chat> getByChatType(ChatType chatType) {
        return fetchChatsByChatType(chatType);
    }
    
    private List<Chat> fetchChatsByChatType(ChatType chatType) {
        final String query = "SELECT * FROM " + tableName + " WHERE chat_type = ?";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatType);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats of type {}", chats.size(), chatType);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error fetching chats by type: {}", chatType, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Chat> getByParticipantId(int userId) {
        return fetchChatsByParticipantId(userId);
    }
    
    /**
     * Asynchronously gets all chats for a specific participant.
     * 
     * @param userId The ID of the user to get chats for
     * @return CompletableFuture that resolves to a list of chats the user is a participant in
     */
    public CompletableFuture<List<Chat>> getByParticipantIdAsync(int userId) {
        // Execute async database operation
        return executeWithConnectionAsync(connection -> {
            final String query = "SELECT c.* FROM " + tableName + " c " +
                                "JOIN " + CHAT_PARTICIPANTS_TABLE + " cp ON c.id = cp.chat_id " +
                                "WHERE cp.user_id = ?";
            
            List<Chat> chats = new ArrayList<>();
            
            try (PreparedStatement statement = prepare(connection, query, userId);
                 ResultSet rs = statement.executeQuery()) {
                
                while (rs.next()) {
                    chats.add(mapResultSetToChat(rs));
                }
            }
            
            logger.trace("Async fetched {} chats for user ID {}", chats.size(), userId);
            
            return chats;
        });
    }
    
    private List<Chat> fetchChatsByParticipantId(int userId) {
        final String query = "SELECT c.* FROM " + tableName + " c " +
                            "JOIN " + CHAT_PARTICIPANTS_TABLE + " cp ON c.id = cp.chat_id " +
                            "WHERE cp.user_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, userId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats for user ID {}", chats.size(), userId);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error fetching chats by participant ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Chat> searchByNamePattern(String namePattern) {
        String pattern = "%" + namePattern + "%";
        
        return fetchChatsByNamePattern(pattern);
    }
    
    private List<Chat> fetchChatsByNamePattern(String pattern) {
        final String query = "SELECT * FROM " + tableName + " WHERE name ILIKE ?";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, pattern);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats matching pattern '{}'", chats.size(), pattern);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error searching chats by name pattern: {}", pattern, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Maps a ResultSet to a Chat object.
     */
    private Chat mapResultSetToChat(ResultSet rs) throws SQLException {
        return new Chat(
            rs.getInt("id"),
            ChatType.valueOf(rs.getString("chat_type")),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("created_at")
        );
    }

    /**
     * Gets chats of a specific type that a user is not a member of.
     *
     * @param chatType The type of chat to filter by
     * @param userId The ID of the user 
     * @return List of chats of the specified type that the user is not a member of
     */
    public List<Chat> getChatsByTypeNotJoinedByUser(ChatType chatType, int userId) {
        final String query = "SELECT c.* FROM " + tableName + " c WHERE c.chat_type = ? " +
                "AND c.name NOT LIKE '%[DELETED]%' " +
                "AND c.id NOT IN (SELECT chat_id FROM " + CHAT_PARTICIPANTS_TABLE + " WHERE user_id = ?)";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatType, userId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats by type {} not joined by user {}", chats.size(), chatType, userId);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error fetching chats by type {} not joined by user {}", chatType, userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Asynchronously gets chats of a specific type that a user is not a member of.
     *
     * @param chatType The type of chat to filter by
     * @param userId The ID of the user
     * @return CompletableFuture with list of chats of the specified type that the user is not a member of
     */
    public CompletableFuture<List<Chat>> getChatsByTypeNotJoinedByUserAsync(ChatType chatType, int userId) {
        return CompletableFuture.supplyAsync(() -> getChatsByTypeNotJoinedByUser(chatType, userId));
    }

    /**
     * Gets all public chats (CHANEL and GROUP types) that are not marked as deleted.
     * 
     * @return A list of all public chats
     */
    @Override
    public List<Chat> getPublicChats() {
        final String query = "SELECT * FROM " + tableName + 
                " WHERE (chat_type = ? OR chat_type = ?) AND name NOT LIKE '%[DELETED]%'";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, 
                        ChatType.CHANEL.toString(), ChatType.GROUP.toString());
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} public chats", chats.size());
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error fetching public chats", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches for public chats of a specific type with name or description containing the query.
     *
     * @param chatType The type of chat to filter by
     * @param query The search query for name or description
     * @return List of matching chats
     */
    @Override
    public List<Chat> searchPublicChatsByTypeAndQuery(ChatType chatType, String query) {
        final String sqlQuery = "SELECT * FROM " + tableName + 
                " WHERE chat_type = ? AND name NOT LIKE '%[DELETED]%' " +
                "AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?)";
        
        String searchPattern = "%" + query.toLowerCase() + "%";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, sqlQuery, 
                        chatType, searchPattern, searchPattern);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats of type {} matching query '{}'", 
                        chats.size(), chatType, query);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error searching chats by type {} and query '{}'", chatType, query, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches for public chats of a specific type that a user is not a member of,
     * with name or description containing the query.
     *
     * @param chatType The type of chat to filter by
     * @param userId The ID of the user
     * @param query The search query for name or description
     * @return List of matching chats the user has not joined
     */
    public List<Chat> searchPublicChatsByTypeNotJoinedByUser(ChatType chatType, int userId, String query) {
        final String sqlQuery = "SELECT * FROM " + tableName + " c " +
                "WHERE c.chat_type = ? AND c.name NOT LIKE '%[DELETED]%' " +
                "AND (LOWER(c.name) LIKE ? OR LOWER(c.description) LIKE ?) " +
                "AND c.id NOT IN (SELECT chat_id FROM " + CHAT_PARTICIPANTS_TABLE + " WHERE user_id = ?)";
        
        String searchPattern = "%" + query.toLowerCase() + "%";
        
        try {
            return executeWithConnection(connection -> {
                List<Chat> chats = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, sqlQuery, 
                        chatType, searchPattern, searchPattern, userId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        chats.add(mapResultSetToChat(rs));
                    }
                }
                
                logger.trace("Fetched {} chats of type {} matching query '{}' not joined by user {}", 
                        chats.size(), chatType, query, userId);
                return chats;
            });
        } catch (SQLException e) {
            logger.error("Error searching chats by type {} and query '{}' not joined by user {}", 
                    chatType, query, userId, e);
            return new ArrayList<>();
        }
    }
}
