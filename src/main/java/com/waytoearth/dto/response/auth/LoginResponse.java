package com.waytoearth.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "사용자 ID", example = "12345")
    private Long userId;

    @Schema(description = "액세스 토큰 (15분)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰 (30일)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "신규 사용자 여부", example = "true")
    private Boolean isNewUser;

    @Schema(description = "온보딩 완료 여부", example = "false")
    private Boolean isOnboardingCompleted;
}