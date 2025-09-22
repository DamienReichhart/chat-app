package com.ap4.common.utils;

import com.ap4.common.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    public void testValidateUserValidUser() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertTrue(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserNullUser() {
        assertFalse(ValidationUtil.validateUser(null));
    }
    
    @Test
    public void testValidateUser_emptyUsername() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserBlankUsername() {
        User user = new User();
        user.setUsername("   ");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserNullUsername() {
        User user = new User();
        user.setUsername(null);
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserEmptyPassword() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("");
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserBlankPassword() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("   ");
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserNullPassword() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword(null);
        user.setEmail("test@example.com");
        
        assertFalse(ValidationUtil.validateUser(user));
    }
    
    @Test
    public void testValidateUserNullEmail() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password123");
        user.setEmail(null);
        
        // Email can be null according to the validation logic
        assertTrue(ValidationUtil.validateUser(user));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"valid message", "test", "123", " "})
    public void testValidateMessageNotNullNotEmpty(String message) {
        assertFalse(ValidationUtil.validateMessage(message));
    }
    
    @Test
    public void testValidateMessageNullMessage() {
        assertTrue(ValidationUtil.validateMessage(null));
    }
    
    @Test
    public void testValidateMessageEmptyMessage() {
        assertTrue(ValidationUtil.validateMessage(""));
    }
} 