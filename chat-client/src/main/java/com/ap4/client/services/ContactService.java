package com.ap4.client.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.interfaces.services.IChatParticipantService;
import com.ap4.client.interfaces.services.IChatService;
import com.ap4.client.interfaces.services.IContactService;
import com.ap4.client.interfaces.services.IMessageService;
import com.ap4.client.interfaces.services.IUserService;
import com.ap4.common.enums.ChatType;
import com.ap4.common.enums.ContentType;
import com.ap4.common.enums.Role;
import com.ap4.common.models.Chat;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.Message;
import com.ap4.common.models.User;

/**
 * Implementation of the ContactService interface.
 * Manages direct chat functionality and user contacts.
 */
public class ContactService implements IContactService {
    private static final Logger logger = LogManager.getLogger(ContactService.class);
    
    private final IChatService chatService;
    private final IChatParticipantService participantService;
    private final IUserService userService;
    private final IMessageService messageService;
    
    /**
     * Default constructor.
     */
    public ContactService() {
        this.chatService = new ChatService();
        this.participantService = new ChatParticipantService();
        this.userService = new UserService();
        this.messageService = new MessageService();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public ContactService(IChatService chatService, IChatParticipantService participantService, 
                         IUserService userService, IMessageService messageService) {
        this.chatService = chatService;
        this.participantService = participantService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public Chat createDirectChat(User currentUser, User contactUser) {
        if (!validateDirectChatCreation(currentUser, contactUser)) {
            throw new IllegalArgumentException("Invalid direct chat request");
        }
        
        // Check if a direct chat already exists
        Chat existingChat = getDirectChat(currentUser, contactUser);
        if (existingChat != null) {
            logger.debug("Direct chat already exists between users: {} and {}", 
                        currentUser.getUsername(), contactUser.getUsername());
            return existingChat;
        }
        
        // Generate a unique name for the direct chat (not visible to users)
        String chatName = "direct_" + currentUser.getId() + "_" + contactUser.getId();
        
        // Create new direct chat
        Chat newChat = new Chat();
        newChat.setName(chatName);
        newChat.setChatType(ChatType.INDIVIDUAL);
        newChat.setDescription("Direct chat between " + currentUser.getUsername() + 
                             " and " + contactUser.getUsername());
        chatService.createChat(newChat);

        // Get the created chat with its ID
        newChat = chatService.getChatByName(chatName);
        
        // Add both users to the chat
        chatService.addParticipant(currentUser, newChat, Role.OWNER);
        chatService.addParticipant(contactUser, newChat, Role.OWNER);

        // Create system message
        Message message = new Message();
        message.setChat(newChat);
        message.setContentType(ContentType.TEXT);
        message.setSender(currentUser);
        message.setPinned(false);
        message.setContent("Chat started");
        messageService.createMessage(message);
        
        logger.info("Direct chat created between users: {} and {}", 
                   currentUser.getUsername(), contactUser.getUsername());
        return newChat;
    }

    @Override
    public Chat getDirectChat(User currentUser, User contactUser) {
        if (currentUser == null || contactUser == null) {
            return null;
        }
        
        // Get all chats for current user
        List<Chat> userChats = chatService.getChatsByUserId(currentUser.getId())
                .stream()
                .filter(chat -> chat.getChatType() == ChatType.INDIVIDUAL)
                .collect(Collectors.toList());
        
        // Find the direct chat with the contact user
        for (Chat chat : userChats) {
            List<ChatParticipant> participants = participantService.getParticipantsByChatId(chat.getId());
            
            if (participants.size() == 2) {
                boolean foundContact = false;
                boolean foundCurrentUser = false;
                
                for (ChatParticipant participant : participants) {
                    if (participant.getUser().getId() == contactUser.getId()) {
                        foundContact = true;
                    }
                    if (participant.getUser().getId() == currentUser.getId()) {
                        foundCurrentUser = true;
                    }
                }
                
                if (foundContact && foundCurrentUser) {
                    return chat;
                }
            }
        }
        
        return null;
    }

    @Override
    public List<Chat> getDirectChats(User currentUser) {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return chatService.getChatsByUserId(currentUser.getId())
                .stream()
                .filter(chat -> chat.getChatType() == ChatType.INDIVIDUAL)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getPotentialContacts(User currentUser) {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // Get all users
        List<User> allUsers = userService.searchUsers("");
        
        // Get existing contacts
        List<User> existingContacts = getContacts(currentUser);
        
        // Filter out current user and existing contacts
        return allUsers.stream()
                .filter(user -> user.getId() != currentUser.getId() && 
                               !existingContacts.contains(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getContacts(User currentUser) {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        List<User> contacts = new ArrayList<>();
        List<Chat> directChats = getDirectChats(currentUser);
        
        for (Chat chat : directChats) {
            List<ChatParticipant> participants = participantService.getParticipantsByChatId(chat.getId());
            
            for (ChatParticipant participant : participants) {
                if (participant.getUser().getId() != currentUser.getId()) {
                    contacts.add(participant.getUser());
                    break;
                }
            }
        }
        
        return contacts;
    }

    @Override
    public List<User> searchPotentialContacts(String query, User currentUser) {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // Search users by query
        List<User> searchResults = userService.searchUsers(query);
        
        // Get existing contacts
        List<User> existingContacts = getContacts(currentUser);
        
        // Filter out current user and existing contacts
        return searchResults.stream()
                .filter(user -> user.getId() != currentUser.getId() && 
                               !existingContacts.contains(user))
                .collect(Collectors.toList());
    }

    @Override
    public void removeDirectChat(int chatId, User currentUser) {
        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            logger.warn("Remove direct chat failed: chat not found: {}", chatId);
            throw new IllegalArgumentException("Chat not found");
        }
        
        // Check if chat is a direct chat
        if (chat.getChatType() != ChatType.INDIVIDUAL) {
            logger.warn("Remove direct chat failed: chat is not a direct chat: {}", chatId);
            throw new IllegalArgumentException("This is not a direct chat");
        }
        
        // Check if user is in the chat
        if (participantService.getParticipant(currentUser.getId(), chat.getId()) == null) {
            logger.warn("Remove direct chat failed: user not in chat: {}", currentUser.getUsername());
            throw new SecurityException("You don't have permission to remove this chat");
        }
        
        // Mark the chat as deleted
        chatService.markChatAsDeleted(chatId);
        
        logger.info("Direct chat removed: {}", chat.getName());
    }

    @Override
    public boolean validateDirectChatCreation(User currentUser, User contactUser) {
        if (currentUser == null) {
            logger.debug("Direct chat validation failed: current user is null");
            throw new IllegalArgumentException("Current user cannot be null");
        }
        
        if (contactUser == null) {
            logger.debug("Direct chat validation failed: contact user is null");
            throw new IllegalArgumentException("Contact user cannot be null");
        }
        
        if (currentUser.getId() == contactUser.getId()) {
            logger.debug("Direct chat validation failed: cannot create chat with self");
            throw new IllegalArgumentException("Cannot create a direct chat with yourself");
        }
        
        return true;
    }
} 