package com.waytoearth.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카카오 로그인 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequest {

    @Schema(description = "카카오 인가 코드", example = "abc123xyz", required = true)
    @NotBlank(message = "인가 코드는 필수입니다")
    private String code;
}
