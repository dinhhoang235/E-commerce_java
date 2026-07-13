package com.hoang.backend.common.exceptions;

public class UserNotFoundException extends IllegalArgumentException {
    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}
