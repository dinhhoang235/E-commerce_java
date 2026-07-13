package com.hoang.backend.common.exceptions;

public class InsufficientStockException extends IllegalArgumentException {
    public InsufficientStockException(Long variantId, int available, int requested) {
        super("Insufficient stock for variant " + variantId + ". Available: " + available + ", Requested: " + requested);
    }
}
