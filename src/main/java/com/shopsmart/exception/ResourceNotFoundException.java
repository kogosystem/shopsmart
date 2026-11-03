package com.shopsmart.exception;

/** Thrown when a product, order, category etc. does not exist. Maps to a 404 page. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
