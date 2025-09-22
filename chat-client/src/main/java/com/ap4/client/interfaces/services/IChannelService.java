package com.ap4.client.interfaces.services;

import java.util.List;

import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

/**
 * Service interface for channel-related operations.
 * Provides methods for creating, editing, joining, and managing channels.
 */
public interface IChannelService {
    
    /**
     * Creates a new channel with the given name and description.
     *
     * @param channelName The name of the new channel
     * @param description The description of the new channel
     * @param creator The user creating the channel
     * @return The newly created channel
     * @throws IllegalArgumentException if the input data is invalid
     */
    Chat createChannel(String channelName, String description, User creator);
    
    /**
     * Updates an existing channel with new information.
     *
     * @param channelId The ID of the channel to update
     * @param channelName The new name for the channel
     * @param description The new description for the channel
     * @param currentUser The user performing the update
     * @return The updated channel
     * @throws IllegalArgumentException if the update data is invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    Chat updateChannel(int channelId, String channelName, String description, User currentUser);
    
    /**
     * Adds a user to a channel.
     *
     * @param channelId The ID of the channel
     * @param userToAdd The user to add to the channel
     * @param currentUser The user performing the action
     * @throws IllegalArgumentException if the inputs are invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    void addUserToChannel(int channelId, User userToAdd, User currentUser);
    
    /**
     * Removes a user from a channel.
     *
     * @param channelId The ID of the channel
     * @param userToRemove The user to remove from the channel
     * @param currentUser The user performing the action
     * @throws IllegalArgumentException if the inputs are invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    void removeUserFromChannel(int channelId, User userToRemove, User currentUser);
    
    /**
     * Gets a list of all channels available to the current user.
     *
     * @param currentUser The current user
     * @return A list of channels available to the user
     */
    List<Chat> getAvailableChannels(User currentUser);
    
    /**
     * Gets a list of users in a channel.
     *
     * @param channelId The ID of the channel
     * @return A list of users in the channel
     * @throws IllegalArgumentException if the channel ID is invalid
     */
    List<User> getChannelUsers(int channelId);
    
    /**
     * Gets a list of participants in a channel with their roles.
     *
     * @param channelId The ID of the channel
     * @return A list of chat participants with user and role information
     * @throws IllegalArgumentException if the channel ID is invalid
     */
    List<ChatParticipant> getChannelParticipants(int channelId);
    
    /**
     * Join a channel with the given channel ID.
     *
     * @param channelId The ID of the channel to join
     * @param user The user joining the channel
     * @return The joined channel
     * @throws IllegalArgumentException if the channel does not exist or is not joinable
     */
    Chat joinChannel(int channelId, User user);

    /**
     * Leave a channel.
     *
     * @param channelId The ID of the channel to leave
     * @param user The user leaving the channel
     * @throws IllegalArgumentException if the channel does not exist
     * @throws SecurityException if the user is the owner and cannot leave
     */
    void leaveChannel(int channelId, User user);
    
    /**
     * Validates channel name and description.
     *
     * @param channelName The name to validate
     * @param description The description to validate
     * @return true if the data is valid
     * @throws IllegalArgumentException with a specific message if validation fails
     */
    boolean validateChannelData(String channelName, String description);
    
    /**
     * Gets a channel by its ID.
     *
     * @param channelId The ID of the channel
     * @return The channel if found, null otherwise
     * @throws IllegalArgumentException if the channel ID is invalid
     */
    Chat getChannelById(int channelId);
    
    /**
     * Searches for channels matching the search term.
     *
     * @param searchTerm The term to search for in channel names and descriptions
     * @param currentUser The current user
     * @return A list of channels matching the search criteria
     */
    List<Chat> searchChannels(String searchTerm, User currentUser);
    
    /**
     * Finds a channel by name from a list of available channels.
     *
     * @param channelName The name of the channel to find
     * @param availableChannels The list of channels to search in
     * @return The channel if found, null otherwise
     */
    Chat findChannelByName(String channelName, List<Chat> availableChannels);
} 