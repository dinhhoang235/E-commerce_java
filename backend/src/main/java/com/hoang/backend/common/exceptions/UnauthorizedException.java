package com.hoang.backend.common.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("You do not have permission to access this resource.");
    }
}
