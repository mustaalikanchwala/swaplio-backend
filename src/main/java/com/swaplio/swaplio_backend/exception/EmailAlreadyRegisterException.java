package com.swaplio.swaplio_backend.exception;

public class EmailAlreadyRegisterException extends RuntimeException {
    public EmailAlreadyRegisterException(String message) {
        super(message);
    }
}
