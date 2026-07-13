package com.hoang.backend.common.exceptions;

public class InvalidOrderStatusException extends IllegalArgumentException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
