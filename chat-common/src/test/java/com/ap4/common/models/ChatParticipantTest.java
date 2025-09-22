package com.ap4.common.models;

import com.ap4.common.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatParticipantTest {

    @Test
    public void testChatParticipantGettersAndSetters() {
        ChatParticipant participant = new ChatParticipant();
        final User user = new User();
        user.setId(1);
        final Chat chat = new Chat();
        chat.setId(1);


        participant.setId(1);
        participant.setChat(chat);
        participant.setUser(user);
        participant.setRole(Role.ADMIN);

        assertEquals(1, participant.getId());
        assertEquals(chat, participant.getChat());
        assertEquals(user, participant.getUser());
        assertEquals(Role.ADMIN, participant.getRole());
    }

    @Test
    public void testChatParticipantConstructor() {
        final User user = new User();
        user.setId(1);
        final Chat chat = new Chat();
        chat.setId(1);

        Date now = new Date();

        ChatParticipant participant = new ChatParticipant(1, now , user, chat, Role.MEMBER);

        assertEquals(1, participant.getId());
        assertEquals(now, participant.getJoinedAt());
        assertEquals(chat, participant.getChat());
        assertEquals(user, participant.getUser());
        assertEquals(Role.MEMBER, participant.getRole());
    }

    @Test
    public void testParticipantToString() {
        final User user = new User();
        user.setId(1);
        final Chat chat = new Chat();
        chat.setId(1);

        Date now = new Date();

        ChatParticipant participant = new ChatParticipant(1, now , user, chat, Role.MEMBER);

        String expected = "ChatParticipant{id=1, joinedAt=" + now + ", user=" + user + ", chat=" + chat + ", role=MEMBER}";
        assertEquals(expected, participant.toString());
    }
}
