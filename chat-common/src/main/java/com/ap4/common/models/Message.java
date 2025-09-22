package com.ap4.common.models;

import java.util.Date;

import com.ap4.common.enums.ContentType;

public class Message {
    int id;
    String content;
    ContentType contentType;
    byte[] fileData;
    String fileName;
    String fileType;
    Date timestamp;
    boolean pinned;
    boolean anonymous;
    boolean deleted;
    Chat chat;
    User sender;

    public Message() {
    }

    public Message(int id, String content, ContentType contentType, Date timestamp, boolean pinned, boolean anonymous, Chat chat, User sender) {
        this.id = id;
        this.content = content;
        this.contentType = contentType;
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.anonymous = anonymous;
        this.chat = chat;
        this.sender = sender;
    }
    
    public Message(int id, String content, ContentType contentType, byte[] fileData, String fileName, String fileType, Date timestamp, boolean pinned, boolean anonymous, Chat chat, User sender) {
        this.id = id;
        this.content = content;
        this.contentType = contentType;
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.anonymous = anonymous;
        this.chat = chat;
        this.sender = sender;
    }
    
    // For backward compatibility
    public Message(int id, String content, ContentType contentType, Date timestamp, boolean pinned, Chat chat, User sender) {
        this(id, content, contentType, timestamp, pinned, false, chat, sender);
    }
    
    // For backward compatibility
    public Message(int id, String content, ContentType contentType, byte[] fileData, boolean pinned, Chat chat, User sender) {
        this(id, content, contentType, fileData, null, null, null, pinned, false, chat, sender);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public boolean isFileMessage() {
        return contentType == ContentType.FILE || contentType == ContentType.IMAGE;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    
    public boolean isAnonymous() {
        return anonymous;
    }
    
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", contentType=" + contentType +
                ", fileName='" + (fileName != null ? fileName : "") + '\'' +
                ", fileType='" + (fileType != null ? fileType : "") + '\'' +
                ", timestamp=" + timestamp +
                ", pinned=" + pinned +
                ", anonymous=" + anonymous +
                ", deleted=" + deleted +
                ", chat=" + chat +
                ", sender=" + sender +
                '}';
    }
}
