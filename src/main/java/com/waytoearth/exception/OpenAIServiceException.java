package com.waytoearth.exception;

/**
 * OpenAI API 호출 관련 예외
 */
public class OpenAIServiceException extends RuntimeException {
    public OpenAIServiceException(String message) {
        super(message);
    }

    public OpenAIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
