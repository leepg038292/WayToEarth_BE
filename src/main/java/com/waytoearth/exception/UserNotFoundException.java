package com.waytoearth.exception;

/**
 * 사용자를 찾을 수 없을 때 예외
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다: " + userId);
    }
}