package com.waytoearth.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...", required = true)
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}
