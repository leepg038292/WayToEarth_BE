package com.waytoearth.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

    @Schema(description = "새로 발급된 액세스 토큰 (15분)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "새로 발급된 리프레시 토큰 (30일)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
