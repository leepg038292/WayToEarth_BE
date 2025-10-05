package com.waytoearth.exception;

/**
 * 러닝 세션을 찾을 수 없을 때 예외
 */
public class RunningSessionNotFoundException extends RuntimeException {
    public RunningSessionNotFoundException(String message) {
        super(message);
    }

    public RunningSessionNotFoundException(String sessionId, String details) {
        super("러닝 세션을 찾을 수 없습니다. sessionId: " + sessionId + " - " + details);
    }
}
