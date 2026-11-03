package com.shopsmart.exception;

/**
 * Thrown for broken business rules the customer should see as a friendly
 * message: empty cart at checkout, not enough stock, duplicate email, etc.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
