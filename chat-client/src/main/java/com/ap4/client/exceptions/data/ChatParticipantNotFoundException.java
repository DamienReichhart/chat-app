package com.ap4.client.exceptions.data;

/**
 * Exception thrown when trying to access a chat participant that doesn't exist.
 */
public class ChatParticipantNotFoundException extends DataAccessException {
    
    public ChatParticipantNotFoundException(int participantId) {
        super("Chat participant with ID " + participantId + " not found");
    }
    
    public ChatParticipantNotFoundException(int userId, int chatId) {
        super("Chat participant with user ID " + userId + " in chat ID " + chatId + " not found");
    }
    
    public ChatParticipantNotFoundException(String message) {
        super(message);
    }
    
    public ChatParticipantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 