package com.ap4.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IGroupService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Implementation of the IGroupService interface.
 * Provides functionality for group-related operations such as
 * creating, editing, joining, and managing groups.
 */
public class GroupService implements IGroupService {
    private static final Logger logger = LogManager.getLogger(GroupService.class);
    
    private final IChatService chatService;
    private final IChatParticipantService chatParticipantService;
    private final IUserService userService;
    private final IMessageService messageService;
    
    /**
     * Default constructor that initializes required services.
     */
    public GroupService() {
        this.chatService = new ChatService();
        this.chatParticipantService = new ChatParticipantService();
        this.userService = new UserService();
        this.messageService = new MessageService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     *
     * @param chatService The chat service to use
     * @param chatParticipantService The chat participant service to use
     * @param userService The user service to use
     * @param messageService The message service to use
     */
    public GroupService(IChatService chatService, IChatParticipantService chatParticipantService, 
                        IUserService userService, IMessageService messageService) {
        this.chatService = chatService;
        this.chatParticipantService = chatParticipantService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public Chat createGroup(String groupName, String description, User creator) {
        if (creator == null) {
            logger.error("Cannot create group with null creator");
            throw new IllegalArgumentException("Creator is required");
        }
        
        // Validate inputs
        if (!validateGroupData(groupName, description)) {
            return null;
        }
        
        // Check if a group with this name already exists
        Chat existingChat = chatService.getChatByName(groupName);
        if (existingChat != null) {
            logger.error("Group with name '{}' already exists", groupName);
            throw new IllegalArgumentException("A group with this name already exists");
        }
        
        try {
            // Create the new group
            Chat newGroup = new Chat();
            newGroup.setName(groupName);
            newGroup.setDescription(description);
            newGroup.setChatType(ChatType.GROUP);
            
            // Save the group
            Chat createdGroup = chatService.createChat(newGroup);
            
            // Add the creator as owner
            chatParticipantService.addParticipant(creator, createdGroup, Role.OWNER);
            
            logger.info("Created group '{}' with ID: {}", groupName, createdGroup.getId());
            return createdGroup;
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create group: " + e.getMessage(), e);
        }
    }

    @Override
    public Chat updateGroup(int groupId, String groupName, String description, User currentUser) {
        if (currentUser == null) {
            logger.error("Cannot update group with null user");
            throw new IllegalArgumentException("User is required");
        }
        
        // Validate inputs
        if (!validateGroupData(groupName, description)) {
            return null;
        }
        
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Check if current user has permission to edit
            boolean hasPermission = chatParticipantService.hasRole(currentUser.getId(), groupId, Role.OWNER) ||
                                   chatParticipantService.hasRole(currentUser.getId(), groupId, Role.ADMIN);
            
            if (!hasPermission) {
                logger.error("User {} does not have permission to edit group {}", currentUser.getId(), groupId);
                throw new SecurityException("You don't have permission to edit this group");
            }
            
            // Check if name is being changed and if the new name is available
            if (!group.getName().equals(groupName)) {
                Chat existingChat = chatService.getChatByName(groupName);
                if (existingChat != null && existingChat.getId() != groupId) {
                    logger.error("Group with name '{}' already exists", groupName);
                    throw new IllegalArgumentException("A group with this name already exists");
                }
            }
            
            // Update the group
            Chat updatedGroup = new Chat();
            updatedGroup.setId(groupId);
            updatedGroup.setName(groupName);
            updatedGroup.setDescription(description);
            updatedGroup.setChatType(ChatType.GROUP);
            
            chatService.updateChat(updatedGroup);
            logger.info("Updated group with ID {}: name='{}', description='{}'", 
                        groupId, groupName, description);
            
            return updatedGroup;
        } catch (Exception e) {
            logger.error("Error updating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update group: " + e.getMessage(), e);
        }
    }

    @Override
    public void addUserToGroup(int groupId, User userToAdd, User currentUser) {
        if (userToAdd == null || currentUser == null) {
            logger.error("Cannot add user to group with null user references");
            throw new IllegalArgumentException("User references cannot be null");
        }
        
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Check if current user has permission to add users
            boolean hasPermission = chatParticipantService.hasRole(currentUser.getId(), groupId, Role.OWNER) ||
                                   chatParticipantService.hasRole(currentUser.getId(), groupId, Role.ADMIN);
            
            if (!hasPermission) {
                logger.error("User {} does not have permission to add users to group {}", 
                            currentUser.getId(), groupId);
                throw new SecurityException("You don't have permission to add users to this group");
            }
            
            // Check if user is already in the group
            ChatParticipant existingParticipant = chatParticipantService.getParticipant(userToAdd.getId(), groupId);
            if (existingParticipant != null) {
                logger.warn("User {} is already in group {}", userToAdd.getId(), groupId);
                return; // User is already in the group, no action needed
            }
            
            // Add the user to the group
            chatParticipantService.addParticipant(userToAdd, group, Role.MEMBER);
            
            logger.info("Added user {} to group {}", userToAdd.getId(), groupId);
        } catch (Exception e) {
            logger.error("Error adding user to group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add user to group: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeUserFromGroup(int groupId, User userToRemove, User currentUser) {
        if (userToRemove == null || currentUser == null) {
            logger.error("Cannot remove user from group with null user references");
            throw new IllegalArgumentException("User references cannot be null");
        }
        
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Different validation if user is removing themselves or removing someone else
            if (userToRemove.getId() == currentUser.getId()) {
                // User is removing themselves (leaving the group)
                leaveGroup(groupId, currentUser);
            } else {
                // User is removing someone else
                
                // Check if current user has permission to remove users
                boolean hasPermission = chatParticipantService.hasRole(currentUser.getId(), groupId, Role.OWNER) ||
                                       chatParticipantService.hasRole(currentUser.getId(), groupId, Role.ADMIN);
                
                if (!hasPermission) {
                    logger.error("User {} does not have permission to remove users from group {}", 
                                currentUser.getId(), groupId);
                    throw new SecurityException("You don't have permission to remove users from this group");
                }
                
                // Can't remove the owner unless you're also an owner
                boolean isUserOwner = chatParticipantService.hasRole(userToRemove.getId(), groupId, Role.OWNER);
                boolean isCurrentUserOwner = chatParticipantService.hasRole(currentUser.getId(), groupId, Role.OWNER);
                
                if (isUserOwner && !isCurrentUserOwner) {
                    logger.error("Cannot remove the owner from group {}", groupId);
                    throw new SecurityException("Cannot remove the owner from the group");
                }
                
                // Remove the user from the group
                chatParticipantService.removeParticipant(userToRemove.getId(), groupId);
                logger.info("Removed user {} from group {}", userToRemove.getId(), groupId);
            }
        } catch (Exception e) {
            logger.error("Error removing user from group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove user from group: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Chat> getAvailableGroups(User currentUser) {
        if (currentUser == null) {
            logger.error("Cannot get available groups for null user");
            throw new IllegalArgumentException("User is required");
        }
        
        try {
            // Get all public groups
            List<Chat> allGroups = chatService.getPublicChatsByType(ChatType.GROUP);
            
            // Filter to get only joinable groups (all groups that the user is not already a member of)
            List<ChatParticipant> userParticipations = chatParticipantService.getParticipantsByUserId(currentUser.getId());
            List<Integer> userGroupIds = userParticipations.stream()
                .filter(p -> p.getChat().getChatType() == ChatType.GROUP)
                .map(p -> p.getChat().getId())
                .collect(Collectors.toList());
            
            // Return groups the user is not part of
            List<Chat> availableGroups = allGroups.stream()
                .filter(g -> !userGroupIds.contains(g.getId()))
                .collect(Collectors.toList());
            
            logger.debug("Found {} available groups for user {}", availableGroups.size(), currentUser.getId());
            return availableGroups;
        } catch (Exception e) {
            logger.error("Error getting available groups: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list on error
        }
    }

    @Override
    public List<Chat> searchAvailableGroups(String searchTerm, User currentUser) {
        if (currentUser == null) {
            logger.error("Cannot search available groups for null user");
            throw new IllegalArgumentException("User is required");
        }
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // If search term is empty, just return all available groups
            return getAvailableGroups(currentUser);
        }
        
        try {
            // Get all available groups first
            List<Chat> availableGroups = getAvailableGroups(currentUser);
            
            // Filter groups by search term
            String searchTermLower = searchTerm.toLowerCase().trim();
            
            List<Chat> searchResults = availableGroups.stream()
                .filter(chat -> {
                    String name = chat.getName() != null ? chat.getName().toLowerCase() : "";
                    String desc = chat.getDescription() != null ? chat.getDescription().toLowerCase() : "";
                    return name.contains(searchTermLower) || desc.contains(searchTermLower);
                })
                .collect(Collectors.toList());
            
            logger.debug("Found {} groups matching search term '{}' for user {}", 
                         searchResults.size(), searchTerm, currentUser.getId());
            return searchResults;
        } catch (Exception e) {
            logger.error("Error searching groups: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list on error
        }
    }

    @Override
    public List<User> getGroupUsers(int groupId) {
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Get all participants for this group
            List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(groupId);
            
            // Extract just the users
            List<User> users = participants.stream()
                .map(ChatParticipant::getUser)
                .collect(Collectors.toList());
            
            logger.debug("Found {} users in group {}", users.size(), groupId);
            return users;
        } catch (Exception e) {
            logger.error("Error getting group users: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list on error
        }
    }

    @Override
    public Chat joinGroup(int groupId, User user) {
        if (user == null) {
            logger.error("Cannot join group with null user");
            throw new IllegalArgumentException("User is required");
        }
        
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Check if user is already in the group
            ChatParticipant existingParticipant = chatParticipantService.getParticipant(user.getId(), groupId);
            if (existingParticipant != null) {
                logger.warn("User {} is already in group {}", user.getId(), groupId);
                return group; // User is already in the group, return the group
            }
            
            // Add the user to the group
            chatParticipantService.addParticipant(user, group, Role.MEMBER);
            
            logger.info("User {} joined group {}", user.getId(), groupId);
            
            return group;
        } catch (Exception e) {
            logger.error("Error joining group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to join group: " + e.getMessage(), e);
        }
    }

    @Override
    public void leaveGroup(int groupId, User user) {
        if (user == null) {
            logger.error("Cannot leave group with null user");
            throw new IllegalArgumentException("User is required");
        }
        
        try {
            // Check if the group exists
            Chat group = chatService.getChatById(groupId);
            if (group == null) {
                logger.error("Group with ID {} not found", groupId);
                throw new IllegalArgumentException("Group not found");
            }
            
            // Check if the group is actually a group
            if (group.getChatType() != ChatType.GROUP) {
                logger.error("Chat with ID {} is not a group", groupId);
                throw new IllegalArgumentException("Chat is not a group");
            }
            
            // Check if user is in the group
            ChatParticipant participant = chatParticipantService.getParticipant(user.getId(), groupId);
            if (participant == null) {
                logger.warn("User {} is not in group {}", user.getId(), groupId);
                return; // User is not in the group, no action needed
            }
            
            // Check if user is the owner
            if (chatParticipantService.hasRole(user.getId(), groupId, Role.OWNER)) {
                // Count other participants
                List<ChatParticipant> participants = chatParticipantService.getParticipantsByChatId(groupId);
                if (participants.size() > 1) {
                    logger.error("Owner cannot leave group {} with other members", groupId);
                    throw new SecurityException("As owner, you cannot leave the group while other members exist. Transfer ownership first or remove all other members.");
                }
                
                // If owner is the only member, delete the group instead of leaving
                List<Message> messages = messageService.getMessagesByChatId(groupId);
                for (Message message : messages) {
                    messageService.markMessageAsDeleted(message.getId());
                }
                
                chatParticipantService.removeParticipant(user.getId(), groupId);
                chatService.markChatAsDeleted(groupId);
                logger.info("Owner {} left and deleted empty group {}", user.getId(), groupId);
                return;
            }
            
            // Regular user leaving - just remove them
            chatParticipantService.removeParticipant(user.getId(), groupId);
            logger.info("User {} left group {}", user.getId(), groupId);
        } catch (Exception e) {
            logger.error("Error leaving group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to leave group: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateGroupData(String groupName, String description) {
        if (groupName == null || groupName.trim().isEmpty()) {
            logger.error("Group name is required");
            throw new IllegalArgumentException("Group name is required");
        }
        
        if (groupName.length() < 3 || groupName.length() > 30) {
            logger.error("Group name must be between 3 and 30 characters");
            throw new IllegalArgumentException("Group name must be between 3 and 30 characters");
        }
        
        if (description != null && description.length() > 200) {
            logger.error("Group description cannot exceed 200 characters");
            throw new IllegalArgumentException("Group description cannot exceed 200 characters");
        }
        
        return true;
    }
} 