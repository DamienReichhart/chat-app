package com.ap4.common.models;

import com.ap4.common.enums.Role;

import java.util.Date;


public class ChatParticipant {
    int id;
    Date joinedAt;
    User user;
    Chat chat;
    Role role;

    public ChatParticipant() {
    }

    public ChatParticipant(int id, Date joinedAt, User user, Chat chat, Role role) {
        this.id = id;
        this.joinedAt = joinedAt;
        this.user = user;
        this.chat = chat;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ChatParticipant{" +
                "id=" + id +
                ", joinedAt=" + joinedAt +
                ", user=" + user +
                ", chat=" + chat +
                ", role=" + role.toString() +
                '}';
    }
}
