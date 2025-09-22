package com.ap4.common.models;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;

public class UserTest {

    @Test
    public void testGettersAndSetters() {
        Date now = new Date();
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEmail("testemail");
        user.setCreatedAt(now);

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("testpassword", user.getPassword());
        assertEquals("testemail", user.getEmail());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    public void testConstructor() {
        Date now = new Date();
        User user = new User(1, "testuser", "testpassword", "testemail", now);

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("testpassword", user.getPassword());
        assertEquals("testemail", user.getEmail());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    public void testToString() {
        Date now = new Date();
        User user = new User(1, "testuser", "testpassword", "testemail", now);

        String expected = "User{id=1, username='testuser', password='testpassword', email='testemail', createdAt=" + now + "}";
        assertEquals(expected, user.toString());
    }
}
