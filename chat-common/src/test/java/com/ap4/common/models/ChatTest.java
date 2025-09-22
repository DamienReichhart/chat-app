package com.ap4.common.models;

import com.ap4.common.enums.ChatType;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTest {

    @Test
    public void testChatGettersAndSetters() {
        Chat chat = new Chat();
        Date now = new Date();

        chat.setId(1);
        chat.setChatType(ChatType.GROUP);
        chat.setName("Test Chat");
        chat.setDescription("This is a test chat");
        chat.setCreatedAt(now);

        assertEquals(1, chat.getId());
        assertEquals(ChatType.GROUP, chat.getChatType());
        assertEquals("Test Chat", chat.getName());
        assertEquals("This is a test chat", chat.getDescription());
        assertEquals(now, chat.getCreatedAt());
    }

    @Test
    public void testChatConstructor() {
        Date now = new Date();
        Chat chat = new Chat(1, ChatType.CHANEL, "Private Chat", "A private chat", now);

        assertEquals(1, chat.getId());
        assertEquals(ChatType.CHANEL, chat.getChatType());
        assertEquals("Private Chat", chat.getName());
        assertEquals("A private chat", chat.getDescription());
        assertEquals(now, chat.getCreatedAt());
    }

    @Test
    public void testChatToString() {
        Date now = new Date();
        Chat chat = new Chat(1, ChatType.GROUP, "Group Chat", "A group chat", now);

        String expected = "Chat{id=1, chatType=GROUP, name='Group Chat', description='A group chat', createdAt=" + now + "}";
        assertEquals(expected, chat.toString());
    }
}