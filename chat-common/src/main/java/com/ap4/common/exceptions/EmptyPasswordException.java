package com.ap4.common.exceptions;

public class EmptyPasswordException extends Exception {
    public EmptyPasswordException() {
        super("Password cannot be empty");
    }
}
