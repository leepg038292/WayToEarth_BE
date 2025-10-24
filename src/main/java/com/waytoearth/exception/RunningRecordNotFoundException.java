package com.waytoearth.exception;

/**
 * 러닝 기록을 찾을 수 없을 때 예외
 */
public class RunningRecordNotFoundException extends RuntimeException {
    public RunningRecordNotFoundException(String message) {
        super(message);
    }

    public RunningRecordNotFoundException(Long recordId) {
        super("러닝 기록을 찾을 수 없습니다. recordId: " + recordId);
    }
}
