package com.evhub.app.exception;

public class ValidationException extends RuntimeException {
    private final int statusCode;

    public ValidationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
