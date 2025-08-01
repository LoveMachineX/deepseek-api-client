package com.yourcompany.deepseek;

public class ConnectionFailedException extends RuntimeException {
    public ConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}