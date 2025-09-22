package com.ap4.common.dto;

import java.io.Serializable;

import com.ap4.common.models.Message;
import com.ap4.common.models.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for WebSocket messages exchanged between client and server.
 * This DTO encapsulates all the necessary information for different types of WebSocket messages.
 */
public class WebSocketMessageDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The ID of the chat this message is related to
     */
    private int chatId;
    
    /**
     * The message content (may be null for some message types)
     */
    private Message message;
    
    /**
     * The user associated with this message (sender or actor)
     */
    private User user;
    
    /**
     * Error message in case of an error
     */
    private String error;

    /**
     * Default constructor required for Jackson deserialization
     */
    public WebSocketMessageDTO() {
    }

    /**
     * Full constructor for creating a WebSocket message
     * 
     * @param chatId The chat ID
     * @param message The message content (may be null)
     * @param user The user (may be null)
     */
    @JsonCreator
    public WebSocketMessageDTO(
            @JsonProperty("chatId") int chatId, 
            @JsonProperty("message") Message message, 
            @JsonProperty("user") User user) {
        this.chatId = chatId;
        this.message = message;
        this.user = user;
    }

    /**
     * Get the chat ID
     * @return The chat ID
     */
    public int getChatId() {
        return chatId;
    }

    /**
     * Set the chat ID
     * @param chatId The chat ID
     */
    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    /**
     * Get the message content
     * @return The message content
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Set the message content
     * @param message The message content
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Get the user
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the user
     * @param user The user
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Get the error message
     * @return The error message
     */
    public String getError() {
        return error;
    }
    
    /**
     * Set the error message
     * @param error The error message
     */
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "WebSocketMessageDTO{" +
                "chatId=" + chatId +
                ", message=" + (message != null ? "Message[id=" + message.getId() + "]" : "null") +
                ", user=" + (user != null ? "User[id=" + user.getId() + "]" : "null") +
                ", error=" + (error != null ? error : "null") +
                '}';
    }
} 