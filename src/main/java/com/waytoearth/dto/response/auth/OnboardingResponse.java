package com.waytoearth.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "온보딩 완료 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponse {

    @Schema(description = "사용자 ID", example = "12345")
    private Long userId;

    @Schema(description = "완료 메시지", example = "온보딩이 완료되었습니다.")
    private String message;
}