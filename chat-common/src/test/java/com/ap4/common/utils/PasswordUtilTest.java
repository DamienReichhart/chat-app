package com.ap4.common.utils;

import com.ap4.common.exceptions.EmptyHashException;
import com.ap4.common.exceptions.EmptyPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testng.AssertJUnit.*;

public class PasswordUtilTest {

    @Test
    public void testHashPassword() {
        String password = "testPassword";
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Check that the hashed password is not null
        assertNotNull(hashedPassword);

        // Check that the hashed password is not equal to the original password
        assertFalse(hashedPassword.equals(password));
    }

    @Test
    public void testCheckPassword() throws EmptyHashException, EmptyPasswordException {
        String password = "testPassword";
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Check that the password matches the hashed password
        assertTrue(PasswordUtil.checkPassword(password, hashedPassword));

        // Check that a different password does not match
        assertFalse(PasswordUtil.checkPassword("wrongPassword", hashedPassword));

        assertThrows(EmptyPasswordException.class, () -> {
            PasswordUtil.checkPassword(null, hashedPassword);
        });

        assertThrows(EmptyHashException.class, () -> {
            PasswordUtil.checkPassword(password, null);
        });
    }
}
