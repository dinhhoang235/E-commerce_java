package com.hoang.backend.common.exceptions;

public class UnauthorizedException extends IllegalArgumentException {
    public UnauthorizedException() {
        super("You do not have permission to access this resource.");
    }
}
