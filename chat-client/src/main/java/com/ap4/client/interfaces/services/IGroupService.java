package com.ap4.client.interfaces.services;

import java.util.List;

import com.ap4.common.models.Chat;
import com.ap4.common.models.User;

/**
 * Service interface for group-related operations.
 * Provides methods for creating, editing, joining, and managing groups.
 */
public interface IGroupService {
    
    /**
     * Creates a new group with the given name and description.
     *
     * @param groupName The name of the new group
     * @param description The description of the new group
     * @param creator The user creating the group
     * @return The newly created group
     * @throws IllegalArgumentException if the input data is invalid
     */
    Chat createGroup(String groupName, String description, User creator);
    
    /**
     * Updates an existing group with new information.
     *
     * @param groupId The ID of the group to update
     * @param groupName The new name for the group
     * @param description The new description for the group
     * @param currentUser The user performing the update
     * @return The updated group
     * @throws IllegalArgumentException if the update data is invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    Chat updateGroup(int groupId, String groupName, String description, User currentUser);
    
    /**
     * Adds a user to a group.
     *
     * @param groupId The ID of the group
     * @param userToAdd The user to add to the group
     * @param currentUser The user performing the action
     * @throws IllegalArgumentException if the inputs are invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    void addUserToGroup(int groupId, User userToAdd, User currentUser);
    
    /**
     * Removes a user from a group.
     *
     * @param groupId The ID of the group
     * @param userToRemove The user to remove from the group
     * @param currentUser The user performing the action
     * @throws IllegalArgumentException if the inputs are invalid
     * @throws SecurityException if the current user doesn't have permission
     */
    void removeUserFromGroup(int groupId, User userToRemove, User currentUser);
    
    /**
     * Gets a list of all groups available to the current user.
     *
     * @param currentUser The current user
     * @return A list of groups available to the user
     */
    List<Chat> getAvailableGroups(User currentUser);
    
    /**
     * Searches for available groups matching the search term.
     *
     * @param searchTerm The term to search for in group names and descriptions
     * @param currentUser The current user
     * @return A list of groups matching the search term
     */
    List<Chat> searchAvailableGroups(String searchTerm, User currentUser);
    
    /**
     * Gets a list of users in a group.
     *
     * @param groupId The ID of the group
     * @return A list of users in the group
     * @throws IllegalArgumentException if the group ID is invalid
     */
    List<User> getGroupUsers(int groupId);
    
    /**
     * Join a group with the given group ID.
     *
     * @param groupId The ID of the group to join
     * @param user The user joining the group
     * @return The joined group
     * @throws IllegalArgumentException if the group does not exist or is not joinable
     */
    Chat joinGroup(int groupId, User user);

    /**
     * Leave a group.
     *
     * @param groupId The ID of the group to leave
     * @param user The user leaving the group
     * @throws IllegalArgumentException if the group does not exist
     * @throws SecurityException if the user is the owner and cannot leave
     */
    void leaveGroup(int groupId, User user);
    
    /**
     * Validates group name and description.
     *
     * @param groupName The name to validate
     * @param description The description to validate
     * @return true if the data is valid
     * @throws IllegalArgumentException with a specific message if validation fails
     */
    boolean validateGroupData(String groupName, String description);
} 