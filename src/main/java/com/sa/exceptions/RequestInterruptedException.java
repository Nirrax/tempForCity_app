package com.sa.exceptions;

public class RequestInterruptedException extends RuntimeException {
    public RequestInterruptedException(String message) {
        super(message);
    }
}
