package com.waytoearth.exception;

/**
 * 중복된 리소스 예외
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}