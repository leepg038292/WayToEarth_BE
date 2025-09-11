package com.waytoearth.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Mock 로그인 요청 (테스트용)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MockLoginRequest {

    @Schema(description = "테스트할 사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
}