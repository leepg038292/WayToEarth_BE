package com.waytoearth.dto.request.running;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Schema(description = "러닝 완료 요청")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningCompleteRequest {

    @Schema(description = "러닝 세션 ID", example = "b6b2b8b5-2d8d-4e8f-9a8e-7e6c5d4f3a21", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "sessionId는 필수입니다.")
    private String sessionId;

    @Schema(description = "총 이동 거리(미터)", example = "5200", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero(message = "distanceMeters는 0 이상이어야 합니다.")
    private Integer distanceMeters;

    @Schema(description = "총 소요 시간(초)", example = "1800", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "durationSeconds는 0보다 커야 합니다.")
    private Integer durationSeconds;

    @Schema(description = "평균 페이스(초/킬로미터). 서버에서 재계산해도 됨.", example = "347")
    @Positive(message = "averagePaceSeconds는 0보다 커야 합니다.")
    private Integer averagePaceSeconds;

    @Schema(description = "칼로리(kcal). 서버에서 계산/보정 가능", example = "350")
    @PositiveOrZero(message = "calories는 0 이상이어야 합니다.")
    private Integer calories;

    @Schema(description = "경로 좌표 리스트(선택). sequence 오름차순으로 정렬해 주세요.")
    @Valid
    private List<RoutePoint> routePoints;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "경로 좌표 한 점")
    public static class RoutePoint {
        @Schema(description = "위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull private Double latitude;

        @Schema(description = "경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull private Double longitude;

        @Schema(description = "경로 순서(0부터 시작 권장)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 0, message = "sequence는 0 이상이어야 합니다.")
        private Integer sequence;
    }
}
