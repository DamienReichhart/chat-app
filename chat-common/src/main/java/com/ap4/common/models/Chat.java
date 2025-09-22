package com.ap4.common.models;

import com.ap4.common.enums.ChatType;
import java.util.Date;

public class Chat {
    int id;
    ChatType chatType;
    String name;
    String description;
    Date createdAt;

    public Chat() {
    }

    public Chat(int id, ChatType chatType, String name, String description, Date createdAt) {
        this.id = id;
        this.chatType = chatType;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", chatType=" + chatType +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
