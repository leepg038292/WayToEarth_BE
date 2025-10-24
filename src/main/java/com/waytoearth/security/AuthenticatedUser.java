package com.waytoearth.security;

import com.waytoearth.entity.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final Long userId;
    private final UserRole role;

    // 보안을 위한 toString 오버라이드 (로그에 민감정보 노출 방지)
    @Override
    public String toString() {
        return "AuthenticatedUser{userId=" + userId + ", role=" + role + "}";
    }
}