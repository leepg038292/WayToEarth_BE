package com.waytoearth.exception;

/**
 * 권한이 없는 접근 시도 시 발생하는 예외
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}