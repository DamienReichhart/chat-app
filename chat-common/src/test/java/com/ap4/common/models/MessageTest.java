package com.ap4.common.models;

import com.ap4.common.enums.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MessageTest {

    @Test
    public void testGettersAndSetters() {
        User sender = new User();
        Chat chat = new Chat();
        sender.setId(1);
        chat.setId(1);
        Date now = Date.from(Instant.now());

        Message message = new Message();
        message.setId(1);
        message.setChat(chat);
        message.setSender(sender);
        message.setContent("Hello, World!");
        message.setContentType(ContentType.TEXT);
        message.setTimestamp(now);
        message.setPinned(false);
        message.setAnonymous(false);

        assertEquals(1, message.getId());
        assertEquals(sender, message.getSender());
        assertEquals(chat, message.getChat());
        assertEquals(ContentType.TEXT, message.getContentType());
        assertEquals(false, message.isPinned());
        assertEquals(false, message.isAnonymous());
        assertEquals("Hello, World!", message.getContent());
        assertEquals(now, message.getTimestamp());
    }

    @Test
    public void testConstructor() {
        User sender = new User();
        Chat chat = new Chat();
        sender.setId(1);
        chat.setId(1);

        Date now = new Date();

        Message message = new Message(1, "Hello, World!", ContentType.TEXT, now,false, false, chat, sender);

        assertEquals(1, message.getId());
        assertEquals(sender, message.getSender());
        assertEquals(chat, message.getChat());
        assertEquals(ContentType.TEXT, message.getContentType());
        assertEquals(false, message.isPinned());
        assertEquals(false, message.isAnonymous());
        assertEquals("Hello, World!", message.getContent());
        assertEquals(now, message.getTimestamp());
    }
}
