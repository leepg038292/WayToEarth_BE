package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "RunningCompleteRequest", description = "러닝 완료 요청 DTO")
@Getter @Setter
@NoArgsConstructor
public class RunningCompleteRequest {

    @Schema(description = "세션 ID", example = "2f0a7e9b-3f2b-4c32-8b9a-3d3d7fba8f20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String sessionId;

    @Schema(description = "이동 거리(km)", example = "5.21", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    private BigDecimal distance;

    @Schema(description = "소요 시간(초)", example = "1827", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    private Integer duration;

    @Schema(description = "평균 페이스(분:초/km). 전달 없으면 서버가 계산", example = "05:51")
    private String averagePace;

    @Schema(description = "칼로리(kcal). 전달 없으면 서버가 계산", example = "312")
    private Integer calories;

    @Schema(description = "경로 좌표 목록(선택)")
    private List<RoutePoint> route;

    @Getter @Setter @NoArgsConstructor
    public static class RoutePoint {
        @Schema(description = "위도", example = "37.5665")
        private Double latitude;

        @Schema(description = "경도", example = "126.9780")
        private Double longitude;

        @Schema(description = "좌표 순번 (0부터 or 1부터)", example = "0")
        private Integer sequence;

        @Schema(description = "타임스탬프(ms)", example = "1733731200000")
        private Long timestamp;
    }
}


