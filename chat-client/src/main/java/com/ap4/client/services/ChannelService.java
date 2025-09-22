package com.ap4.client.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChannelService;
import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.ContentType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Implementation of the ChannelService interface.
 * Provides methods for creating, editing, joining, and managing channels.
 */
public class ChannelService implements IChannelService {
    private static final Logger logger = LogManager.getLogger(ChannelService.class);
    
    private final IChatService chatService;
    private final IChatParticipantService participantService;
    private final IMessageService messageService;
    
    /**
     * Default constructor.
     */
    public ChannelService() {
        this.chatService = new ChatService();
        this.participantService = new ChatParticipantService();
        this.messageService = new MessageService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public ChannelService(IChatService chatService, IChatParticipantService participantService, 
                        IMessageService messageService) {
        this.chatService = chatService;
        this.participantService = participantService;
        this.messageService = messageService;
    }

    @Override
    public Chat createChannel(String channelName, String description, User creator) {
        if (!validateChannelData(channelName, description)) {
            throw new IllegalArgumentException("Invalid channel data");
        }
        
        // Check if channel with this name already exists
        Chat existingChat = chatService.getChatByName(channelName);
        if (existingChat != null) {
            logger.warn("Channel creation failed: channel name already exists: {}", channelName);
            throw new IllegalArgumentException("Channel with this name already exists");
        }
        
        // Create new channel
        Chat newChat = new Chat();
        newChat.setName(channelName);
        newChat.setChatType(ChatType.CHANEL);
        newChat.setDescription(description);
        chatService.createChat(newChat);

        // Get the created chat with its ID
        newChat = chatService.getChatByName(channelName);
        
        // Add current user as owner of the channel
        chatService.addParticipant(creator, newChat, Role.OWNER);

        // Create system message indicating channel creation
        Message message = new Message();
        message.setChat(newChat);
        message.setContentType(ContentType.TEXT);
        message.setSender(creator);
        message.setPinned(false);
        message.setContent("Channel created");
        messageService.createMessage(message);
        
        logger.info("Channel created: {} by user: {}", channelName, creator.getUsername());
        return newChat;
    }

    @Override
    public Chat updateChannel(int channelId, String channelName, String description, User currentUser) {
        if (!validateChannelData(channelName, description)) {
            throw new IllegalArgumentException("Invalid channel data");
        }
        
        // Get the channel to update
        Chat channel = chatService.getChatById(channelId);
        if (channel == null) {
            logger.warn("Channel update failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Check if user has permission to edit the channel
        if (!participantService.hasRole(currentUser.getId(), channel.getId(), Role.OWNER) && 
            !participantService.hasRole(currentUser.getId(), channel.getId(), Role.ADMIN)) {
            logger.warn("Channel update failed: user does not have permission: {}", currentUser.getUsername());
            throw new SecurityException("You don't have permission to edit this channel");
        }
        
        // Check if channel name already exists (for a different channel)
        Chat existingChat = chatService.getChatByName(channelName);
        if (existingChat != null && existingChat.getId() != channelId) {
            logger.warn("Channel update failed: channel name already exists: {}", channelName);
            throw new IllegalArgumentException("Channel with this name already exists");
        }
        
        // Update channel
        channel.setName(channelName);
        channel.setDescription(description);
        chatService.updateChat(channel);
        
        logger.info("Channel updated: {} by user: {}", channelName, currentUser.getUsername());
        return channel;
    }

    @Override
    public void addUserToChannel(int channelId, User userToAdd, User currentUser) {
        // Get the channel
        Chat channel = chatService.getChatById(channelId);
        if (channel == null) {
            logger.warn("Add user to channel failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Check if user has permission to add users to the channel
        if (!participantService.hasRole(currentUser.getId(), channel.getId(), Role.OWNER) && 
            !participantService.hasRole(currentUser.getId(), channel.getId(), Role.ADMIN)) {
            logger.warn("Add user to channel failed: user does not have permission: {}", currentUser.getUsername());
            throw new SecurityException("You don't have permission to add users to this channel");
        }
        
        // Check if user is already in the channel
        if (participantService.getParticipant(userToAdd.getId(), channel.getId()) != null) {
            logger.warn("Add user to channel failed: user already in channel: {}", userToAdd.getUsername());
            throw new IllegalArgumentException("User is already in this channel");
        }
        
        // Add user to channel with default role
        chatService.addParticipant(userToAdd, channel, Role.MEMBER);
        
        // Create system message
        Message message = new Message();
        message.setChat(channel);
        message.setContentType(ContentType.TEXT);
        message.setSender(currentUser);
        message.setPinned(false);
        message.setContent(userToAdd.getUsername() + " has been added to the channel");
        messageService.createMessage(message);
        
        logger.info("User added to channel: {} to channel: {} by user: {}", 
                   userToAdd.getUsername(), channel.getName(), currentUser.getUsername());
    }

    @Override
    public void removeUserFromChannel(int channelId, User userToRemove, User currentUser) {
        // Get the channel
        Chat channel = chatService.getChatById(channelId);
        if (channel == null) {
            logger.warn("Remove user from channel failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Check if user has permission to remove users from the channel
        if (!participantService.hasRole(currentUser.getId(), channel.getId(), Role.OWNER) && 
            !participantService.hasRole(currentUser.getId(), channel.getId(), Role.ADMIN)) {
            logger.warn("Remove user from channel failed: user does not have permission: {}", currentUser.getUsername());
            throw new SecurityException("You don't have permission to remove users from this channel");
        }
        
        // Cannot remove an owner unless you are an owner
        if (participantService.hasRole(userToRemove.getId(), channel.getId(), Role.OWNER) && 
            !participantService.hasRole(currentUser.getId(), channel.getId(), Role.OWNER)) {
            logger.warn("Remove user from channel failed: cannot remove an owner: {}", userToRemove.getUsername());
            throw new SecurityException("You don't have permission to remove an owner");
        }
        
        // Check if user is in the channel
        if (participantService.getParticipant(userToRemove.getId(), channel.getId()) == null) {
            logger.warn("Remove user from channel failed: user not in channel: {}", userToRemove.getUsername());
            throw new IllegalArgumentException("User is not in this channel");
        }
        
        // Remove user from channel
        chatService.removeParticipant(userToRemove.getId(), channel.getId());
        
        // Create system message
        Message message = new Message();
        message.setChat(channel);
        message.setContentType(ContentType.TEXT);
        message.setSender(currentUser);
        message.setPinned(false);
        message.setContent(userToRemove.getUsername() + " has been removed from the channel");
        messageService.createMessage(message);
        
        logger.info("User removed from channel: {} from channel: {} by user: {}", 
                   userToRemove.getUsername(), channel.getName(), currentUser.getUsername());
    }

    @Override
    public List<Chat> getAvailableChannels(User currentUser) {
        List<Chat> allChannels = chatService.getPublicChatsByType(ChatType.CHANEL);
        return allChannels;
    }

    @Override
    public List<User> getChannelUsers(int channelId) {
        logger.info("Getting users for channel: {}", channelId);
        
        // Get channel participants and extract only the users
        return participantService.getParticipantsByChatId(channelId).stream()
                .map(participant -> participant.getUser())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatParticipant> getChannelParticipants(int channelId) {
        logger.info("Getting participants with roles for channel: {}", channelId);
        
        // Get the channel to ensure it exists
        Chat channel = getChannelById(channelId);
        if (channel == null) {
            logger.warn("Get channel participants failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Delegate to the participant service to get full participant information
        List<ChatParticipant> participants = participantService.getParticipantsByChatId(channelId);
        logger.info("Retrieved {} participants from service for channel {}", participants.size(), channelId);
        
        // Debug participant data
        for (ChatParticipant participant : participants) {
            User user = participant.getUser();
            if (user == null) {
                logger.warn("Participant has null user reference: {}", participant);
                continue;
            }
            
            logger.debug("Participant: {} ({}), Role: {}", 
                user.getUsername(), user.getId(), participant.getRole());
            
            // Ensure each participant has a reference to the channel
            if (participant.getChat() == null) {
                logger.debug("Setting channel reference for participant {}", participant.getId());
                participant.setChat(channel);
            }
        }
        
        logger.debug("Final participant list size for channel {}: {}", channelId, participants.size());
        return participants;
    }

    @Override
    public Chat joinChannel(int channelId, User user) {
        Chat channel = chatService.getChatById(channelId);
        if (channel == null) {
            logger.warn("Join channel failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Check if the chat is a channel
        if (channel.getChatType() != ChatType.CHANEL) {
            logger.warn("Join channel failed: chat is not a channel: {}", channelId);
            throw new IllegalArgumentException("This is not a channel");
        }
        
        // Check if user is already in the channel
        if (participantService.getParticipant(user.getId(), channel.getId()) != null) {
            logger.warn("Join channel failed: user already in channel: {}", user.getUsername());
            throw new IllegalArgumentException("You are already in this channel");
        }
        
        // Add user to channel with member role
        chatService.addParticipant(user, channel, Role.MEMBER);
        
        // Create system message
        Message message = new Message();
        message.setChat(channel);
        message.setContentType(ContentType.TEXT);
        message.setSender(user);
        message.setPinned(false);
        message.setContent(user.getUsername() + " has joined the channel");
        messageService.createMessage(message);
        
        logger.info("User joined channel: {} to channel: {}", user.getUsername(), channel.getName());
        return channel;
    }

    @Override
    public void leaveChannel(int channelId, User user) {
        Chat channel = chatService.getChatById(channelId);
        if (channel == null) {
            logger.warn("Leave channel failed: channel not found: {}", channelId);
            throw new IllegalArgumentException("Channel not found");
        }
        
        // Check if the chat is a channel
        if (channel.getChatType() != ChatType.CHANEL) {
            logger.warn("Leave channel failed: chat is not a channel: {}", channelId);
            throw new IllegalArgumentException("This is not a channel");
        }
        
        // Check if user is in the channel
        if (participantService.getParticipant(user.getId(), channel.getId()) == null) {
            logger.warn("Leave channel failed: user not in channel: {}", user.getUsername());
            throw new IllegalArgumentException("You are not in this channel");
        }
        
        // Check if user is the last owner
        if (participantService.hasRole(user.getId(), channel.getId(), Role.OWNER)) {
            List<User> owners = participantService.getParticipantsByChatId(channelId)
                .stream()
                .filter(p -> p.getRole() == Role.OWNER)
                .map(p -> p.getUser())
                .collect(Collectors.toList());
                
            if (owners.size() <= 1) {
                logger.warn("Leave channel failed: last owner cannot leave: {}", user.getUsername());
                throw new SecurityException("You are the last owner. You must transfer ownership before leaving");
            }
        }
        
        // Remove user from channel
        chatService.removeParticipant(user.getId(), channel.getId());
        
        // Create system message
        Message message = new Message();
        message.setChat(channel);
        message.setContentType(ContentType.TEXT);
        message.setSender(user);
        message.setPinned(false);
        message.setContent(user.getUsername() + " has left the channel");
        messageService.createMessage(message);
        
        logger.info("User left channel: {} from channel: {}", user.getUsername(), channel.getName());
    }

    @Override
    public boolean validateChannelData(String channelName, String description) {
        if (channelName == null || channelName.trim().isEmpty()) {
            logger.debug("Channel validation failed: name is empty");
            throw new IllegalArgumentException("Channel name cannot be empty");
        }
        
        if (channelName.length() > 50) {
            logger.debug("Channel validation failed: name is too long");
            throw new IllegalArgumentException("Channel name cannot be longer than 50 characters");
        }
        
        if (description != null && description.length() > 255) {
            logger.debug("Channel validation failed: description is too long");
            throw new IllegalArgumentException("Channel description cannot be longer than 255 characters");
        }
        
        return true;
    }
    
    @Override
    public Chat getChannelById(int channelId) {
        Chat chat = chatService.getChatById(channelId);
        
        if (chat == null) {
            logger.warn("Channel not found with ID: {}", channelId);
            return null;
        }
        
        // Check if it's a channel
        if (chat.getChatType() != ChatType.CHANEL) {
            logger.warn("Chat with ID {} is not a channel", channelId);
            return null;
        }
        
        return chat;
    }
    
    @Override
    public List<Chat> searchChannels(String searchTerm, User currentUser) {
        logger.info("Searching channels with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Return all available channels if search term is empty
            return getAvailableChannels(currentUser);
        }
        
        // Get all available channels first
        List<Chat> allChannels = getAvailableChannels(currentUser);
        
        // Filter the channels based on the search term (case-insensitive)
        String searchTermLower = searchTerm.toLowerCase();
        return allChannels.stream()
            .filter(channel -> 
                (channel.getName() != null && 
                 channel.getName().toLowerCase().contains(searchTermLower)) || 
                (channel.getDescription() != null && 
                 channel.getDescription().toLowerCase().contains(searchTermLower))
            )
            .collect(Collectors.toList());
    }
    
    @Override
    public Chat findChannelByName(String channelName, List<Chat> availableChannels) {
        logger.debug("Finding channel by name: {}", channelName);
        
        if (channelName == null || channelName.trim().isEmpty() || availableChannels == null) {
            logger.warn("Invalid parameters for finding channel by name");
            return null;
        }
        
        return availableChannels.stream()
            .filter(c -> c.getName() != null && c.getName().equals(channelName))
            .findFirst()
            .orElse(null);
    }
} 