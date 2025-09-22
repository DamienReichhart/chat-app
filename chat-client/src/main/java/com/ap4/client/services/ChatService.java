package com.ap4.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.ap4.client.db.dao.ChatDAO;
import com.ap4.client.exceptions.data.ChatNotFoundException;
import com.ap4.client.exceptions.data.DataCreationException;
import com.ap4.client.exceptions.data.DataUpdateException;
import com.ap4.client.exceptions.db.EntityRelationshipException;
import com.ap4.client.exceptions.data.InvalidDataException;
import com.ap4.client.interfaces.db.dao.IChatDAO;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service class for chat-related operations.
 * Handles creation, retrieval, and management of chats and chat participants.
 */
public class ChatService implements IChatService {
    private static final Logger logger = LogManager.getLogger(ChatService.class);
    
    private final IChatDAO chatDAO;
    private final IChatParticipantService participantService;

    /**
     * Constructor with dependency injection for the DAOs.
     */
    public ChatService() {
        this.chatDAO = new ChatDAO();
        this.participantService = new ChatParticipantService();
    }

    /**
     * Gets all chats for a specific user.
     *
     * @param userId The ID of the user to get chats for
     * @return List of chats the user is a participant in
     */
    public List<Chat> getChatsByUserId(int userId) {
        // Use the appropriate DAO method based on the actual implementation
        return chatDAO.getByParticipantId(userId);
    }
    
    /**
     * Asynchronously gets all chats for a specific user.
     *
     * @param userId the ID of the user to get chats for
     * @return a CompletableFuture that will provide a list of chats the user is a participant in
     */
    @Override
    public CompletableFuture<List<Chat>> getChatsByUserIdAsync(int userId) {
        // Call the async DAO method to get chats by participant ID
        return ((ChatDAO) chatDAO).getByParticipantIdAsync(userId);
    }

    /**
     * Gets all public chats.
     *
     * @return List of public chats
     */
    public List<Chat> getPublicChats() {
        // Use the direct DAO method for getting public chats
        return chatDAO.getPublicChats();
    }

    /**
     * Gets all public chats of a specific type.
     *
     * @param chatType the type of chat to filter by
     * @return a list of public chats of the specified type
     */
    @Override
    public List<Chat> getPublicChatsByType(ChatType chatType) {
        // Use the DAO method directly instead of streaming through all chats
        return chatDAO.getByChatType(chatType).stream()
                .filter(chat -> !chat.getName().contains("[DELETED]"))
                .collect(Collectors.toList());
    }
    
    /**
     * Asynchronously gets all public chats of a specific type.
     *
     * @param chatType the type of chat to filter by
     * @return a CompletableFuture that will provide a list of public chats of the specified type
     */
    @Override
    public CompletableFuture<List<Chat>> getPublicChatsByTypeAsync(ChatType chatType) {
        return CompletableFuture.supplyAsync(() -> getPublicChatsByType(chatType));
    }
    
    /**
     * Gets all public chats of a specific type that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @return a list of public chats of the specified type that the user is not a member of
     */
    @Override
    public List<Chat> getPublicChatsByTypeNotJoinedByUser(ChatType chatType, int userId) {
        // Use the optimized DAO method to get the data in a single database operation
        return ((ChatDAO) chatDAO).getChatsByTypeNotJoinedByUser(chatType, userId);
    }
    
    /**
     * Asynchronously gets all public chats of a specific type that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @return a CompletableFuture that will provide a list of public chats of the specified type that the user is not a member of
     */
    @Override
    public CompletableFuture<List<Chat>> getPublicChatsByTypeNotJoinedByUserAsync(ChatType chatType, int userId) {
        return CompletableFuture.supplyAsync(() -> getPublicChatsByTypeNotJoinedByUser(chatType, userId));
    }

    /**
     * Gets a chat by its ID.
     *
     * @param chatId The ID of the chat to retrieve
     * @return The chat if found
     * @throws ChatNotFoundException if the chat doesn't exist
     * @throws IllegalArgumentException if the chat ID is invalid
     */
    public Chat getChatById(int chatId) {
        if (chatId <= 0) {
            throw new IllegalArgumentException("Chat ID must be positive");
        }
        
        Chat chat = chatDAO.getById(chatId);
        if (chat == null) {
            throw new ChatNotFoundException(chatId);
        }
        
        return chat;
    }
    
    /**
     * Asynchronously gets a chat by its ID.
     *
     * @param chatId the ID of the chat to retrieve
     * @return a CompletableFuture that will provide the chat if found; {@code null} otherwise
     */
    @Override
    public CompletableFuture<Chat> getChatByIdAsync(int chatId) {
        // Call the async DAO method to get chat by ID
        return ((ChatDAO) chatDAO).getByIdAsync(chatId);
    }

    /**
     * Gets a chat by its name.
     *
     * @param chatName The name of the chat to retrieve
     * @return The chat if found, null otherwise
     */
    public Chat getChatByName(String chatName) {
        return chatDAO.getByName(chatName);
    }

    /**
     * Creates a new chat.
     *
     * @param chat The chat to create
     * @return The created chat
     * @throws DataCreationException if the chat couldn't be created
     * @throws InvalidDataException if the chat data is invalid
     */
    public Chat createChat(Chat chat) {
        if (chat == null) {
            throw new InvalidDataException("Chat", "entity", "cannot be null");
        }
        
        if (chat.getName() == null || chat.getName().trim().isEmpty()) {
            throw new InvalidDataException("Chat", "name", "cannot be empty");
        }
        
        try {
            // Check if a chat with this name already exists
            Chat existingChat = chatDAO.getByName(chat.getName());
            if (existingChat != null) {
                throw new DataCreationException("A chat with the name '" + chat.getName() + "' already exists");
            }
            
            chatDAO.create(chat);
            logger.info("Created new chat: {} (ID: {})", chat.getName(), chat.getId());
            return chat;
        } catch (DataCreationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create chat: {}", e.getMessage(), e);
            throw new DataCreationException("Failed to create chat: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing chat.
     *
     * @param chat The chat to update
     * @throws ChatNotFoundException if the chat doesn't exist
     * @throws DataUpdateException if the chat couldn't be updated
     * @throws InvalidDataException if the chat data is invalid
     */
    public void updateChat(Chat chat) {
        if (chat == null) {
            throw new InvalidDataException("Chat", "entity", "cannot be null");
        }
        
        if (chat.getId() <= 0) {
            throw new InvalidDataException("Chat", "id", "must be positive");
        }
        
        if (chat.getName() == null || chat.getName().trim().isEmpty()) {
            throw new InvalidDataException("Chat", "name", "cannot be empty");
        }
        
        try {
            // Check if the chat exists
            Chat existingChat = getChatById(chat.getId());
            
            // Check if a different chat with this name already exists
            Chat chatWithSameName = chatDAO.getByName(chat.getName());
            if (chatWithSameName != null && chatWithSameName.getId() != chat.getId()) {
                throw new DataUpdateException("Another chat with the name '" + chat.getName() + "' already exists");
            }
            
            chatDAO.update(chat);
            logger.info("Updated chat: {} (ID: {})", chat.getName(), chat.getId());
        } catch (ChatNotFoundException e) {
            throw e;
        } catch (DataUpdateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update chat: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to update chat: " + e.getMessage(), e);
        }
    }

    /**
     * Marks a chat as deleted.
     * This is a soft delete operation that updates the name with a "[DELETED]" prefix.
     *
     * @param chatId The ID of the chat to mark as deleted
     * @return true if successful, false otherwise
     * @throws ChatNotFoundException if the chat doesn't exist
     * @throws DataUpdateException if there's an error updating the chat
     * @throws IllegalArgumentException if the chat ID is invalid
     */
    public boolean markChatAsDeleted(int chatId) {
        if (chatId <= 0) {
            throw new IllegalArgumentException("Chat ID must be positive");
        }
        
        try {
            Chat chat = getChatById(chatId); // This will throw ChatNotFoundException if chat doesn't exist
            
            // Perform soft delete by marking the name
            chat.setName("[DELETED] " + chat.getName());
            chatDAO.update(chat);
            
            logger.info("Marked chat as deleted: {} (ID: {})", chat.getName(), chat.getId());
            return true;
        } catch (ChatNotFoundException e) {
            // Just rethrow specific exceptions
            logger.warn("Failed to mark chat as deleted - not found: {}", e.getMessage());
            throw e;
        } catch (DataUpdateException e) {
            logger.error("Failed to update chat for deletion: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error marking chat as deleted: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to mark chat as deleted: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for chats matching the given query.
     *
     * @param query The search query
     * @return List of chats matching the query
     */
    public List<Chat> searchChats(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getPublicChats();
        }
        
        // Use a more optimized approach that searches at the database level
        List<Chat> chanelResults = ((ChatDAO) chatDAO).searchPublicChatsByTypeAndQuery(ChatType.CHANEL, query);
        List<Chat> groupResults = ((ChatDAO) chatDAO).searchPublicChatsByTypeAndQuery(ChatType.GROUP, query);
        
        List<Chat> combinedResults = new ArrayList<>();
        combinedResults.addAll(chanelResults);
        combinedResults.addAll(groupResults);
        
        return combinedResults;
    }
    
    /**
     * Searches for chats of a specific type matching the given query.
     *
     * @param chatType the type of chat to filter by
     * @param query the search query for name or description
     * @return a list of chats of the specified type matching the query
     */
    @Override
    public List<Chat> searchChatsByTypeAndQuery(ChatType chatType, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getPublicChatsByType(chatType);
        }
        
        return ((ChatDAO) chatDAO).searchPublicChatsByTypeAndQuery(chatType, query);
    }
    
    /**
     * Asynchronously searches for chats of a specific type matching the given query.
     *
     * @param chatType the type of chat to filter by
     * @param query the search query for name or description
     * @return a CompletableFuture that will provide a list of chats of the specified type matching the query
     */
    @Override
    public CompletableFuture<List<Chat>> searchChatsByTypeAndQueryAsync(ChatType chatType, String query) {
        return CompletableFuture.supplyAsync(() -> searchChatsByTypeAndQuery(chatType, query));
    }

    /**
     * Gets all participants in a chat.
     *
     * @param chatId The ID of the chat
     * @return List of chat participants
     */
    public List<ChatParticipant> getParticipantsByChatId(int chatId) {
        return participantService.getParticipantsByChatId(chatId);
    }

    /**
     * Adds a participant to a chat.
     *
     * @param user The user to add
     * @param chat The chat to add the user to
     * @param role The role of the user in the chat
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws EntityRelationshipException if the user is already a participant in the chat
     */
    public void addParticipant(User user, Chat chat, Role role) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (chat == null) {
            throw new IllegalArgumentException("Chat cannot be null");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        try {
            // Check if the participant already exists in this chat
            if (participantService.getParticipant(user.getId(), chat.getId()) != null) {
                throw new EntityRelationshipException(
                    "User is already a participant in this chat",
                    "User[" + user.getId() + "]", 
                    "Chat[" + chat.getId() + "]"
                );
            }
            
            participantService.addParticipant(user, chat, role);
            logger.info("Added user {} to chat {} with role {}", user.getUsername(), chat.getName(), role);
        } catch (EntityRelationshipException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to add participant: {}", e.getMessage(), e);
            throw new DataCreationException("Failed to add participant: " + e.getMessage(), e);
        }
    }

    /**
     * Updates a participant's role in a chat.
     *
     * @param user The user to update
     * @param chat The chat to update the user's role in
     * @param newRole The new role for the user
     * @return true if the update was successful
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws EntityRelationshipException if the user is not a participant in the chat
     */
    public boolean updateParticipantRole(User user, Chat chat, Role newRole) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (chat == null) {
            throw new IllegalArgumentException("Chat cannot be null");
        }
        
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        try {
            // Check if the participant exists
            if (participantService.getParticipant(user.getId(), chat.getId()) == null) {
                throw new EntityRelationshipException(
                    "User is not a participant in this chat",
                    "User[" + user.getId() + "]", 
                    "Chat[" + chat.getId() + "]"
                );
            }
            
            boolean result = participantService.updateParticipantRole(user.getId(), chat.getId(), newRole);
            logger.info("Updated user {} role in chat {} to {}: {}", 
                     user.getUsername(), chat.getName(), newRole, result ? "success" : "failed");
            return result;
        } catch (EntityRelationshipException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update participant role: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to update participant role: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a user from a chat.
     *
     * @param userId The ID of the user to remove
     * @param chatId The ID of the chat to remove the user from
     * @return true if successful, false otherwise
     */
    public boolean removeParticipant(int userId, int chatId) {
        return participantService.removeParticipant(userId, chatId);
    }

    /**
     * Checks if a user has permission to perform an action in a chat.
     *
     * @param userId The ID of the user
     * @param chatId The ID of the chat
     * @param requiredRole The minimum role required for the action
     * @return true if the user has sufficient permissions, false otherwise
     */
    public boolean hasPermission(int userId, int chatId, Role requiredRole) {
        return participantService.hasPermission(userId, chatId, requiredRole);
    }

    @Override
    public CompletableFuture<List<Chat>> getPublicChatsAsync() {
        return CompletableFuture.supplyAsync(this::getPublicChats);
    }
    
    @Override
    public CompletableFuture<Chat> getChatByNameAsync(String chatName) {
        return CompletableFuture.supplyAsync(() -> getChatByName(chatName));
    }
    
    @Override
    public CompletableFuture<Void> createChatAsync(Chat chat) {
        return CompletableFuture.runAsync(() -> createChat(chat));
    }
    
    @Override
    public CompletableFuture<Void> updateChatAsync(Chat chat) {
        return CompletableFuture.runAsync(() -> updateChat(chat));
    }
    
    @Override
    public CompletableFuture<Boolean> markChatAsDeletedAsync(int chatId) {
        return CompletableFuture.supplyAsync(() -> markChatAsDeleted(chatId));
    }
    
    @Override
    public CompletableFuture<List<Chat>> searchChatsAsync(String query) {
        return CompletableFuture.supplyAsync(() -> searchChats(query));
    }
    
    @Override
    public CompletableFuture<List<ChatParticipant>> getParticipantsByChatIdAsync(int chatId) {
        return CompletableFuture.supplyAsync(() -> getParticipantsByChatId(chatId));
    }
    
    @Override
    public CompletableFuture<Void> addParticipantAsync(User user, Chat chat, Role role) {
        return CompletableFuture.runAsync(() -> addParticipant(user, chat, role));
    }
    
    @Override
    public CompletableFuture<Boolean> updateParticipantRoleAsync(User user, Chat chat, Role newRole) {
        return CompletableFuture.supplyAsync(() -> updateParticipantRole(user, chat, newRole));
    }
    
    @Override
    public CompletableFuture<Boolean> removeParticipantAsync(int userId, int chatId) {
        return CompletableFuture.supplyAsync(() -> removeParticipant(userId, chatId));
    }
    
    @Override
    public CompletableFuture<Boolean> hasPermissionAsync(int userId, int chatId, Role requiredRole) {
        return CompletableFuture.supplyAsync(() -> hasPermission(userId, chatId, requiredRole));
    }

    @Override
    public void deleteChat(int chatId) {
        chatDAO.delete(chatId);
    }

    /**
     * Searches for chats of a specific type matching the given query that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @param query the search query for name or description
     * @return a list of chats of the specified type matching the query that the user is not a member of
     */
    @Override
    public List<Chat> searchChatsByTypeNotJoinedByUser(ChatType chatType, int userId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getPublicChatsByTypeNotJoinedByUser(chatType, userId);
        }
        
        return ((ChatDAO) chatDAO).searchPublicChatsByTypeNotJoinedByUser(chatType, userId, query);
    }
    
    /**
     * Asynchronously searches for chats of a specific type matching the given query that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @param query the search query for name or description
     * @return a CompletableFuture that will provide a list of chats of the specified type matching the query that the user is not a member of
     */
    @Override
    public CompletableFuture<List<Chat>> searchChatsByTypeNotJoinedByUserAsync(ChatType chatType, int userId, String query) {
        return CompletableFuture.supplyAsync(() -> searchChatsByTypeNotJoinedByUser(chatType, userId, query));
    }
}
