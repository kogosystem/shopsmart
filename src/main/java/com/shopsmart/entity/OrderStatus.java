package com.shopsmart.entity;

/** Lifecycle of an order. Admins move orders through these states. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
