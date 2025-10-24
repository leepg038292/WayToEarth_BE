package com.waytoearth.exception;

/**
 * 크루장이 회원 탈퇴를 시도할 때 발생하는 예외
 * 크루장은 먼저 크루장 권한을 이양하거나 크루를 삭제해야 함
 */
public class CrewOwnerCannotDeleteAccountException extends RuntimeException {
    public CrewOwnerCannotDeleteAccountException(String message) {
        super(message);
    }
}
