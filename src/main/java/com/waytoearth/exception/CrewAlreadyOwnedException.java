package com.waytoearth.exception;

/**
 * 사용자가 이미 크루를 소유하고 있을 때 발생하는 예외
 * 한 사용자는 하나의 활성 크루만 소유할 수 있음
 */
public class CrewAlreadyOwnedException extends RuntimeException {
    public CrewAlreadyOwnedException(String message) {
        super(message);
    }
}
