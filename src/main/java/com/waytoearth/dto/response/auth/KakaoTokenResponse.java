package com.waytoearth.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카카오 토큰 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenResponse {

    @Schema(description = "액세스 토큰", example = "access_token_string")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "refresh_token_string")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "bearer")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "만료 시간(초)", example = "7200")
    @JsonProperty("expires_in")
    private Integer expiresIn;
}