package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "RunningStartRequest", description = "러닝 시작 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class RunningStartRequest {

    @Schema(description = "세션 ID(미전달 시 서버에서 생성)", example = "2f0a7e9b-3f2b-4c32-8b9a-3d3d7fba8f20")
    private String sessionId;

    @Schema(description = "러닝 타입 코드", example = "SINGLE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String runningType;

    @Schema(description = "러닝 제목(선택)", example = "아침 러닝")
    private String title;
}
