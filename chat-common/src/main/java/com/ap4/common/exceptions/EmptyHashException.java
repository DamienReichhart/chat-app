package com.ap4.common.exceptions;

public class EmptyHashException extends Exception {
    public EmptyHashException() {
        super("Hash cannot be null or empty");
    }

    public EmptyHashException(String message) {
        super(message);
    }

    public EmptyHashException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyHashException(Throwable cause) {
        super(cause);
    }
}
