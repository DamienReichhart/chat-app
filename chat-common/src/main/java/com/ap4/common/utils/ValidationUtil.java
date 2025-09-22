package com.ap4.common.utils;

import com.ap4.common.models.User;

public class ValidationUtil {

    public static boolean validateUser(User user) {
        if (user == null) {
            return false;
        }
        // Email can be null, so we don't check it

        return !(isNullOrBlank(user.getUsername()) || isNullOrBlank(user.getPassword()));
    }

    public static boolean validateMessage(String message) {
        return !(isNotNullOrEmpty(message));
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    private static boolean isNotNullOrBlank(String str) {
        return !isNullOrBlank(str);
    }
}
