package com.ap4.common.utils;

import com.ap4.common.exceptions.EmptyHashException;
import com.ap4.common.exceptions.EmptyPasswordException;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtil {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashed) throws EmptyPasswordException, EmptyHashException {
        if (password == null) {
            throw new EmptyPasswordException();
        }
        if (hashed == null) {
            throw new EmptyHashException();
        }
        return BCrypt.checkpw(password, hashed);
    }
}
