package com.ap4.client.interfaces.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

/**
 * Interface for chat-related operations.
 * <p>
 * This interface defines methods for the creation, retrieval, and management of chats and chat participants.
 * </p>
 */
public interface IChatService {

    /**
     * Gets all chats for a specific user.
     *
     * @param userId the ID of the user to get chats for
     * @return a list of chats the user is a participant in
     */
    List<Chat> getChatsByUserId(int userId);
    
    /**
     * Asynchronously gets all chats for a specific user.
     *
     * @param userId the ID of the user to get chats for
     * @return a CompletableFuture that will provide a list of chats the user is a participant in
     */
    CompletableFuture<List<Chat>> getChatsByUserIdAsync(int userId);

    /**
     * Gets all public chats.
     *
     * @return a list of public chats
     */
    List<Chat> getPublicChats();
    
    /**
     * Asynchronously gets all public chats.
     *
     * @return a CompletableFuture that will provide a list of public chats
     */
    CompletableFuture<List<Chat>> getPublicChatsAsync();

    /**
     * Gets all public chats of a specific type.
     *
     * @param chatType the type of chat to filter by
     * @return a list of public chats of the specified type
     */
    List<Chat> getPublicChatsByType(ChatType chatType);
    
    /**
     * Asynchronously gets all public chats of a specific type.
     *
     * @param chatType the type of chat to filter by
     * @return a CompletableFuture that will provide a list of public chats of the specified type
     */
    CompletableFuture<List<Chat>> getPublicChatsByTypeAsync(ChatType chatType);
    
    /**
     * Gets all public chats of a specific type that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @return a list of public chats of the specified type that the user is not a member of
     */
    List<Chat> getPublicChatsByTypeNotJoinedByUser(ChatType chatType, int userId);
    
    /**
     * Asynchronously gets all public chats of a specific type that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @return a CompletableFuture that will provide a list of public chats of the specified type that the user is not a member of
     */
    CompletableFuture<List<Chat>> getPublicChatsByTypeNotJoinedByUserAsync(ChatType chatType, int userId);

    /**
     * Gets a chat by its ID.
     *
     * @param chatId the ID of the chat to retrieve
     * @return the chat if found; {@code null} otherwise
     */
    Chat getChatById(int chatId);
    
    /**
     * Asynchronously gets a chat by its ID.
     *
     * @param chatId the ID of the chat to retrieve
     * @return a CompletableFuture that will provide the chat if found; {@code null} otherwise
     */
    CompletableFuture<Chat> getChatByIdAsync(int chatId);

    /**
     * Gets a chat by its name.
     *
     * @param chatName the name of the chat to retrieve
     * @return the chat if found; {@code null} otherwise
     */
    Chat getChatByName(String chatName);
    
    /**
     * Asynchronously gets a chat by its name.
     *
     * @param chatName the name of the chat to retrieve
     * @return a CompletableFuture that will provide the chat if found; {@code null} otherwise
     */
    CompletableFuture<Chat> getChatByNameAsync(String chatName);

    /**
     * Creates a new chat.
     *
     * @param chat the chat to create
     */
    Chat createChat(Chat chat);
    
    /**
     * Asynchronously creates a new chat.
     *
     * @param chat the chat to create
     * @return a CompletableFuture that will indicate the completion of the operation
     */
    CompletableFuture<Void> createChatAsync(Chat chat);

    /**
     * Updates an existing chat.
     *
     * @param chat the chat to update
     */
    void updateChat(Chat chat);
    
    /**
     * Asynchronously updates an existing chat.
     *
     * @param chat the chat to update
     * @return a CompletableFuture that will indicate the completion of the operation
     */
    CompletableFuture<Void> updateChatAsync(Chat chat);

    /**
     * Marks a chat as deleted.
     * <p>
     * This is a soft delete operation that updates the chat's status.
     * </p>
     *
     * @param chatId the ID of the chat to mark as deleted
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean markChatAsDeleted(int chatId);
    
    /**
     * Asynchronously marks a chat as deleted.
     *
     * @param chatId the ID of the chat to mark as deleted
     * @return a CompletableFuture that will provide the result of the operation
     */
    CompletableFuture<Boolean> markChatAsDeletedAsync(int chatId);

    /**
     * Searches for chats matching the given query.
     *
     * @param query the search query
     * @return a list of chats matching the query
     */
    List<Chat> searchChats(String query);
    
    /**
     * Searches for chats of a specific type matching the given query.
     *
     * @param chatType the type of chat to filter by
     * @param query the search query for name or description
     * @return a list of chats of the specified type matching the query
     */
    List<Chat> searchChatsByTypeAndQuery(ChatType chatType, String query);
    
    /**
     * Asynchronously searches for chats of a specific type matching the given query.
     *
     * @param chatType the type of chat to filter by
     * @param query the search query for name or description
     * @return a CompletableFuture that will provide a list of chats of the specified type matching the query
     */
    CompletableFuture<List<Chat>> searchChatsByTypeAndQueryAsync(ChatType chatType, String query);
    
    /**
     * Searches for chats of a specific type matching the given query that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @param query the search query for name or description
     * @return a list of chats of the specified type matching the query that the user is not a member of
     */
    List<Chat> searchChatsByTypeNotJoinedByUser(ChatType chatType, int userId, String query);
    
    /**
     * Asynchronously searches for chats of a specific type matching the given query that the user is not a member of.
     *
     * @param chatType the type of chat to filter by
     * @param userId the ID of the user
     * @param query the search query for name or description
     * @return a CompletableFuture that will provide a list of chats of the specified type matching the query that the user is not a member of
     */
    CompletableFuture<List<Chat>> searchChatsByTypeNotJoinedByUserAsync(ChatType chatType, int userId, String query);

    /**
     * Asynchronously searches for chats matching the given query.
     *
     * @param query the search query
     * @return a CompletableFuture that will provide a list of chats matching the query
     */
    CompletableFuture<List<Chat>> searchChatsAsync(String query);

    /**
     * Gets all participants in a chat.
     *
     * @param chatId the ID of the chat
     * @return a list of chat participants
     */
    List<ChatParticipant> getParticipantsByChatId(int chatId);
    
    /**
     * Asynchronously gets all participants in a chat.
     *
     * @param chatId the ID of the chat
     * @return a CompletableFuture that will provide a list of chat participants
     */
    CompletableFuture<List<ChatParticipant>> getParticipantsByChatIdAsync(int chatId);

    /**
     * Adds a user to a chat with the specified role.
     *
     * @param user the user to add
     * @param chat the chat to add the user to
     * @param role the role for the user in the chat
     */
    void addParticipant(User user, Chat chat, Role role);
    
    /**
     * Asynchronously adds a user to a chat with the specified role.
     *
     * @param user the user to add
     * @param chat the chat to add the user to
     * @param role the role for the user in the chat
     * @return a CompletableFuture that will indicate the completion of the operation
     */
    CompletableFuture<Void> addParticipantAsync(User user, Chat chat, Role role);

    /**
     * Updates the role of a participant in a chat.
     *
     * @param user    the user whose role is to be updated
     * @param chat    the chat in which to update the role
     * @param newRole the new role for the user
     * @return {@code true} if the update is successful; {@code false} otherwise
     */
    boolean updateParticipantRole(User user, Chat chat, Role newRole);
    
    /**
     * Asynchronously updates the role of a participant in a chat.
     *
     * @param user    the user whose role is to be updated
     * @param chat    the chat in which to update the role
     * @param newRole the new role for the user
     * @return a CompletableFuture that will provide the result of the operation
     */
    CompletableFuture<Boolean> updateParticipantRoleAsync(User user, Chat chat, Role newRole);

    /**
     * Removes a user from a chat.
     *
     * @param userId the ID of the user to remove
     * @param chatId the ID of the chat to remove the user from
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean removeParticipant(int userId, int chatId);
    
    /**
     * Asynchronously removes a user from a chat.
     *
     * @param userId the ID of the user to remove
     * @param chatId the ID of the chat to remove the user from
     * @return a CompletableFuture that will provide the result of the operation
     */
    CompletableFuture<Boolean> removeParticipantAsync(int userId, int chatId);

    /**
     * Checks if a user has permission to perform an action in a chat.
     *
     * @param userId       the ID of the user
     * @param chatId       the ID of the chat
     * @param requiredRole the minimum role required for the action
     * @return {@code true} if the user has sufficient permissions; {@code false} otherwise
     */
    boolean hasPermission(int userId, int chatId, Role requiredRole);
    
    /**
     * Asynchronously checks if a user has permission to perform an action in a chat.
     *
     * @param userId       the ID of the user
     * @param chatId       the ID of the chat
     * @param requiredRole the minimum role required for the action
     * @return a CompletableFuture that will provide the result of the operation
     */
    CompletableFuture<Boolean> hasPermissionAsync(int userId, int chatId, Role requiredRole);

    /**
     * Deletes a chat.
     *
     * @param chatId the ID of the chat to delete
     */
    void deleteChat(int chatId);
    
}
