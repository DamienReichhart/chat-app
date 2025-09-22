package com.ap4.client.db.dao;

import com.ap4.client.interfaces.db.dao.IMessageDAO;
import com.ap4.common.models.Message;
import com.ap4.common.models.Chat;
import com.ap4.common.models.User;
import com.ap4.common.enums.ContentType;
import com.ap4.client.interfaces.db.dao.IUserDAO;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.db.DatabaseConnectionException;
import com.ap4.client.exceptions.data.DataAccessException;
import com.ap4.client.exceptions.db.MessageNotFoundException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.data.DataDeletionException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO extends AbstractDAO<Message, Integer> implements IMessageDAO {
    private static final String MESSAGES_TABLE = "messages";
    
    // Cache for users and chats to avoid repeated database queries
    private final Map<Integer, User> userCache = new HashMap<>();
    private final Map<Integer, Chat> chatCache = new HashMap<>();
    
    private final IUserDAO userDAO;
    private final ChatDAO chatDAO;

    public MessageDAO() {
        super(MESSAGES_TABLE);
        this.userDAO = new UserDAO();
        this.chatDAO = new ChatDAO();
    }

    /**
     * Creates a new message record.
     */
    @Override
    public Message create(Message message) {
        if (message == null) {
            throw new InvalidDataException("Message", "Entity cannot be null");
        }
        
        if (message.getChat() == null || message.getChat().getId() <= 0) {
            throw new InvalidDataException("Message", "chat", "must be a valid entity with positive ID");
        }
        
        if (message.getSender() == null || message.getSender().getId() <= 0) {
            throw new InvalidDataException("Message", "sender", "must be a valid entity with positive ID");
        }
        
        final String query = "INSERT INTO " + tableName + 
                " (content, content_type, file_data, file_name, file_type, pinned, anonymous, chat_id, sender_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, message.getContent());
                    statement.setString(2, message.getContentType().name());
                    
                    // Set file data
                    if (message.getFileData() != null) {
                        statement.setBytes(3, message.getFileData());
                    } else {
                        statement.setNull(3, Types.BINARY);
                    }
                    
                    statement.setString(4, message.getFileName());
                    statement.setString(5, message.getFileType());
                    statement.setBoolean(6, message.isPinned());
                    statement.setBoolean(7, message.isAnonymous());
                    statement.setInt(8, message.getChat().getId());
                    statement.setInt(9, message.getSender().getId());
                    
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            message.setId(rs.getInt("id"));
                        } else {
                            throw new DataCreationException("Message creation failed: No ID returned");
                        }
                    }
                    
                    logger.trace("Message created: {}", message);
                    return message;
                }
            });
        } catch (SQLException e) {
            logger.error("Error creating message: {}", message, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("foreign key") && errorMessage.contains("chat_id")) {
                throw new EntityRelationshipException("Chat with ID " + message.getChat().getId() + " does not exist", e);
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("sender_id")) {
                throw new EntityRelationshipException("User with ID " + message.getSender().getId() + " does not exist", e);
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to create message", e);
            } else {
                throw new DataCreationException("Message", e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error creating message: {}", e.getMessage(), e);
            throw new DataCreationException("An unexpected error occurred while creating the message", e);
        }
    }

    /**
     * Updates an existing message.
     */
    @Override
    public Message update(Message message) {
        if (message == null) {
            throw new InvalidDataException("Message", "Entity cannot be null");
        }
        
        if (message.getId() <= 0) {
            throw new InvalidDataException("Message", "id", "must be a positive number");
        }
        
        if (message.getChat() == null || message.getChat().getId() <= 0) {
            throw new InvalidDataException("Message", "chat", "must be a valid entity with positive ID");
        }
        
        if (message.getSender() == null || message.getSender().getId() <= 0) {
            throw new InvalidDataException("Message", "sender", "must be a valid entity with positive ID");
        }
        
        // Check if message exists
        Message existingMessage = fetchMessageById(message.getId());
        if (existingMessage == null) {
            throw new MessageNotFoundException(message.getId());
        }
        
        final String query = "UPDATE " + tableName + 
                " SET content = ?, content_type = ?, file_data = ?, file_name = ?, file_type = ?, " +
                "pinned = ?, anonymous = ?, chat_id = ?, sender_id = ? WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, message.getContent());
                    statement.setString(2, message.getContentType().name());
                    
                    // Set file data
                    if (message.getFileData() != null) {
                        statement.setBytes(3, message.getFileData());
                    } else {
                        statement.setNull(3, Types.BINARY);
                    }
                    
                    statement.setString(4, message.getFileName());
                    statement.setString(5, message.getFileType());
                    statement.setBoolean(6, message.isPinned());
                    statement.setBoolean(7, message.isAnonymous());
                    statement.setInt(8, message.getChat().getId());
                    statement.setInt(9, message.getSender().getId());
                    statement.setInt(10, message.getId());
                    
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new MessageNotFoundException(message.getId());
                    }
                    
                    logger.trace("Message updated: {}", message);
                    return message;
                }
            });
        } catch (MessageNotFoundException e) {
            // Rethrow message not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error updating message: {}", message, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("foreign key") && errorMessage.contains("chat_id")) {
                throw new EntityRelationshipException("Chat with ID " + message.getChat().getId() + " does not exist", e);
            } else if (errorMessage.contains("foreign key") && errorMessage.contains("sender_id")) {
                throw new EntityRelationshipException("User with ID " + message.getSender().getId() + " does not exist", e);
            } else if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to update message", e);
            } else {
                throw new DataUpdateException("Message", message.getId(), e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error updating message: {}", e.getMessage(), e);
            throw new DataUpdateException("Message", message.getId(), "An unexpected error occurred while updating the message");
        }
    }

    /**
     * Deletes a message by its ID.
     */
    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Message", "id", "must be a positive number");
        }
        
        // First get the message to know which chat-related caches to invalidate
        Message message = fetchMessageById(id);
        if (message == null) {
            throw new MessageNotFoundException(id);
        }
        
        final String query = "DELETE FROM " + tableName + " WHERE id = ?";
        
        try {
            boolean result = executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    if (rowsAffected == 0) {
                        throw new MessageNotFoundException(id);
                    }
                    
                    logger.trace("Message deleted, id: {}, rows affected: {}", id, rowsAffected);
                    return true;
                }
            });
            return result;
        } catch (MessageNotFoundException e) {
            // Rethrow message not found exceptions directly
            throw e;
        } catch (SQLException e) {
            logger.error("Error deleting message with id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to delete message", e);
            } else if (errorMessage.contains("foreign key") || errorMessage.contains("reference")) {
                throw new EntityRelationshipException("Cannot delete message because it is referenced by other entities", e);
            } else {
                throw new DataDeletionException("Message", id, e);
            }
        } catch (DataAccessException e) {
            // Just rethrow custom exceptions that were already thrown
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error deleting message: {}", e.getMessage(), e);
            throw new DataDeletionException("Message", id, "An unexpected error occurred while deleting the message");
        }
    }

    /**
     * Retrieves a message by its ID.
     */
    @Override
    public Message getById(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Message", "id", "must be a positive number");
        }
        
        Message message = fetchMessageById(id);
        if (message == null) {
            throw new MessageNotFoundException(id);
        }
        return message;
    }
    
    private Message fetchMessageById(Integer id) {
        final String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, id);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("Message found by id: {}", id);
                        return extractMessageFromResultSet(rs);
                    }
                    
                    logger.trace("No message found for id: {}", id);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching message by id: {}", id, e);
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMessage.contains("connect")) {
                throw new DatabaseConnectionException("Could not connect to the database to fetch message", e);
            } else {
                throw new DataAccessException("Failed to fetch message with ID " + id, e);
            }
        } catch (Exception e) {
            logger.error("Unexpected error fetching message by id: {}", id, e);
            throw new DataAccessException("An unexpected error occurred while fetching the message", e);
        }
    }

    /**
     * Retrieves all messages in a specific chat.
     */
    @Override
    public List<Message> getByChatId(int chatId) {
        return fetchMessagesByChatId(chatId);
    }
    
    private List<Message> fetchMessagesByChatId(int chatId) {
        final String query = "SELECT * FROM " + tableName + " WHERE chat_id = ? ORDER BY timestamp ASC";
        
        try {
            return executeWithConnection(connection -> {
                List<Message> messages = new ArrayList<>();
                
                // First, get all user IDs and chat IDs that we'll need to fetch
                Map<Integer, Integer> userIds = new HashMap<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId)) {
                    // First, collect all the user IDs we need
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            int senderId = rs.getInt("sender_id");
                            userIds.put(senderId, senderId);
                        }
                    }
                }
                
                // Prefetch all users in a single batch
                for (Integer userId : userIds.keySet()) {
                    if (!userCache.containsKey(userId)) {
                        User user = userDAO.getById(userId);
                        if (user != null) {
                            userCache.put(userId, user);
                        }
                    }
                }
                
                // Now get messages with prefetched users and chats
                try (PreparedStatement statement = prepare(connection, query, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        messages.add(extractMessageFromResultSetWithCache(rs));
                    }
                }
                
                logger.trace("Fetched {} messages for chat ID {}", messages.size(), chatId);
                return messages;
            });
        } catch (SQLException e) {
            logger.error("Error fetching messages for chat ID: {}", chatId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all messages sent by a specific user.
     */
    @Override
    public List<Message> getByUserId(int userId) {
        final String query = "SELECT * FROM " + tableName + " WHERE sender_id = ? ORDER BY timestamp DESC";
        
        try {
            return executeWithConnection(connection -> {
                List<Message> messages = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, userId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        messages.add(extractMessageFromResultSet(rs));
                    }
                }
                
                logger.trace("Fetched {} messages for user id {}", messages.size(), userId);
                return messages;
            });
        } catch (SQLException e) {
            logger.error("Error fetching messages by user id: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves messages in a chat with pagination.
     */
    @Override
    public List<Message> getByChatIdPaginated(int chatId, int limit, int offset) {
        final String query = "SELECT * FROM " + tableName + 
                " WHERE chat_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        
        try {
            return executeWithConnection(connection -> {
                List<Message> messages = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId, limit, offset);
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        messages.add(extractMessageFromResultSet(rs));
                    }
                }
                
                logger.trace("Fetched {} messages for chat id {} (paginated)", messages.size(), chatId);
                return messages;
            });
        } catch (SQLException e) {
            logger.error("Error fetching paginated messages for chat id: {}", chatId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves messages in a chat created after a specific date.
     */
    @Override
    public List<Message> getByChatIdAfterDate(int chatId, Date date) {
        final String query = "SELECT * FROM " + tableName + 
                " WHERE chat_id = ? AND timestamp > ? ORDER BY timestamp ASC";
        
        try {
            return executeWithConnection(connection -> {
                List<Message> messages = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId, new java.sql.Timestamp(date.getTime()));
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        messages.add(extractMessageFromResultSet(rs));
                    }
                }
                
                logger.trace("Fetched {} messages for chat id {} after date {}", 
                        messages.size(), chatId, date);
                return messages;
            });
        } catch (SQLException e) {
            logger.error("Error fetching messages by chat id after date: {} - {}", chatId, date, e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves the latest message in a chat.
     */
    @Override
    public Message getLatestByChatId(int chatId) {
        final String query = "SELECT * FROM " + tableName + 
                " WHERE chat_id = ? ORDER BY timestamp DESC LIMIT 1";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        logger.trace("Latest message found for chat id: {}", chatId);
                        return extractMessageFromResultSet(rs);
                    }
                    
                    logger.trace("No messages found for chat id: {}", chatId);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching latest message for chat id: {}", chatId, e);
            return null;
        }
    }

    /**
     * Backward compatibility method for existing service calls
     * @deprecated Use getLatestByChatId instead
     */
    public Message getLastMessageByChatId(int chatId) {
        return getLatestByChatId(chatId);
    }

    /**
     * Retrieves the count of unread messages for a user in a chat.
     */
    @Override
    public int getUnreadCount(int chatId, int userId) {
        final String query = "SELECT COUNT(*) FROM " + tableName + 
                " LEFT JOIN message_read_status ON message_read_status.message_id = " + tableName + ".id " + 
                " AND message_read_status.user_id = ? " +
                " WHERE " + tableName + ".chat_id = ? AND message_read_status.id IS NULL";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, userId, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        logger.trace("Unread count for user {} in chat {}: {}", userId, chatId, count);
                        return count;
                    }
                    
                    return 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error fetching unread count for user {} in chat {}", userId, chatId, e);
            return 0;
        }
    }

    /**
     * Marks all messages in a chat as read for a user.
     */
    @Override
    public int markAllAsRead(int chatId, int userId) {
        final String query = "INSERT INTO message_read_status (message_id, user_id) " +
                "SELECT id, ? FROM " + tableName + " " +
                "WHERE chat_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM message_read_status WHERE message_id = " + tableName + ".id AND user_id = ?)";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, userId, chatId, userId)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    logger.trace("Marked {} messages as read for user id {} in chat id {}", 
                            rowsAffected, userId, chatId);
                    return rowsAffected;
                }
            });
        } catch (SQLException e) {
            logger.error("Error marking messages as read for user {} in chat {}", userId, chatId, e);
            return 0;
        }
    }

    /**
     * Searches for messages containing specific text.
     */
    @Override
    public List<Message> searchByContent(int chatId, String searchText) {
        final String query = "SELECT * FROM " + tableName + 
                " WHERE chat_id = ? AND content ILIKE ? ORDER BY timestamp DESC";
        
        try {
            return executeWithConnection(connection -> {
                List<Message> messages = new ArrayList<>();
                
                try (PreparedStatement statement = prepare(connection, query, chatId, "%" + searchText + "%");
                     ResultSet rs = statement.executeQuery()) {
                    
                    while (rs.next()) {
                        messages.add(extractMessageFromResultSet(rs));
                    }
                }
                
                logger.trace("Found {} messages matching search '{}' in chat {}", 
                        messages.size(), searchText, chatId);
                return messages;
            });
        } catch (SQLException e) {
            logger.error("Error searching messages in chat {}: {}", chatId, searchText, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get only the file data for a specific message.
     * This is a specialized method to avoid loading the entire message into memory.
     */
    public byte[] getFileDataById(int messageId) {
        final String query = "SELECT file_data FROM " + tableName + " WHERE id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, messageId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        byte[] fileData = rs.getBytes("file_data");
                        logger.trace("File data retrieved for message id: {}, size: {} bytes", 
                                messageId, fileData != null ? fileData.length : 0);
                        return fileData;
                    }
                    
                    logger.trace("No file data found for message id: {}", messageId);
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error retrieving file data for message id: {}", messageId, e);
            return null;
        }
    }

    /**
     * Delete all messages for a chat by its name.
     */
    public boolean deleteByChatName(String channelName) {
        // Get the chat ID from the chat name
        ChatDAO chatDAO = new ChatDAO();
        Chat chat = chatDAO.getByName(channelName);
        if (chat == null) {
            return false;
        }
        
        int chatId = chat.getId();
        final String query = "DELETE FROM " + tableName + " WHERE chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatId)) {
                    int rowsAffected = statement.executeUpdate();
                    
                    logger.trace("Deleted {} messages for chat name: {}", rowsAffected, channelName);
                    return rowsAffected > 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error deleting messages for chat name: {}", channelName, e);
            return false;
        }
    }

    /**
     * Get the count of messages in a chat by its name.
     */
    public int getCountByChannelName(String channelName) {
        ChatDAO chatDAO = new ChatDAO();
        Chat chat = chatDAO.getByName(channelName);
        if (chat == null) {
            return 0;
        }
        
        int chatId = chat.getId();
        final String query = "SELECT COUNT(*) FROM " + tableName + " WHERE chat_id = ?";
        
        try {
            return executeWithConnection(connection -> {
                try (PreparedStatement statement = prepare(connection, query, chatId);
                     ResultSet rs = statement.executeQuery()) {
                    
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    
                    return 0;
                }
            });
        } catch (SQLException e) {
            logger.error("Error counting messages for chat name: {}", channelName, e);
            return 0;
        }
    }

    /**
     * Helper methods for user and chat caching
     */
    private User getUserFromCache(int userId) {
        User user = userCache.get(userId);
        if (user == null) {
            user = userDAO.getById(userId);
            if (user != null) {
                userCache.put(userId, user);
            }
        }
        return user;
    }
    
    private Chat getChatFromCache(int chatId) {
        Chat chat = chatCache.get(chatId);
        if (chat == null) {
            chat = chatDAO.getById(chatId);
            if (chat != null) {
                chatCache.put(chatId, chat);
            }
        }
        return chat;
    }
    
    /**
     * Helper to extract a message from a result set, fetching user and chat
     */
    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        int messageId = rs.getInt("id");
        int chatId = rs.getInt("chat_id");
        int senderId = rs.getInt("sender_id");
        
        // Fetch needed objects
        User sender = userDAO.getById(senderId);
        Chat chat = chatDAO.getById(chatId);
        
        if (sender == null || chat == null) {
            logger.error("Failed to find sender or chat for message: {} (sender id: {}, chat id: {})", 
                    messageId, senderId, chatId);
            return null;
        }
        
        boolean anonymous = false;
        try {
            anonymous = rs.getBoolean("anonymous");
        } catch (SQLException e) {
            // Column might not exist in older database versions
            logger.warn("Could not read 'anonymous' field - using default value false");
        }
        
        return new Message(
                messageId,
                rs.getString("content"),
                ContentType.valueOf(rs.getString("content_type")),
                rs.getBytes("file_data"),
                rs.getString("file_name"),
                rs.getString("file_type"),
                rs.getTimestamp("timestamp"),
                rs.getBoolean("pinned"),
                anonymous,
                chat,
                sender
        );
    }
    
    /**
     * Helper to extract a message from a result set, using cached user and chat
     */
    private Message extractMessageFromResultSetWithCache(ResultSet rs) throws SQLException {
        int messageId = rs.getInt("id");
        int chatId = rs.getInt("chat_id");
        int senderId = rs.getInt("sender_id");
        
        // Get from cache or fetch if needed
        User sender = getUserFromCache(senderId);
        Chat chat = getChatFromCache(chatId);
        
        if (sender == null || chat == null) {
            logger.error("Failed to find sender or chat for message: {} (sender id: {}, chat id: {})", 
                    messageId, senderId, chatId);
            return null;
        }
        
        boolean anonymous = false;
        try {
            anonymous = rs.getBoolean("anonymous");
        } catch (SQLException e) {
            // Column might not exist in older database versions
            logger.warn("Could not read 'anonymous' field - using default value false");
        }
        
        return new Message(
                messageId,
                rs.getString("content"),
                ContentType.valueOf(rs.getString("content_type")),
                rs.getBytes("file_data"),
                rs.getString("file_name"),
                rs.getString("file_type"),
                rs.getTimestamp("timestamp"),
                rs.getBoolean("pinned"),
                anonymous,
                chat,
                sender
        );
    }
}
