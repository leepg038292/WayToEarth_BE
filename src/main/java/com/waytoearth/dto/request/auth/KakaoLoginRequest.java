package com.waytoearth.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카카오 로그인 요청 (SDK 방식)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequest {

    @Schema(description = "카카오 액세스 토큰", example = "aaaa1111bbbb2222", required = true)
    @NotBlank(message = "액세스 토큰은 필수입니다")
    private String accessToken;

    @Schema(description = "카카오 사용자 ID", example = "1234567890", required = true) 
    @NotNull(message = "카카오 사용자 ID는 필수입니다")
    private String kakaoId;

    @Schema(description = "모바일 여부", example = "true")
    private Boolean isMobile;
}
