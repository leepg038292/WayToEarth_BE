package com.waytoearth.dto.response.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "러닝 완료 응답")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningCompleteResponse {

    @Schema(description = "러닝 기록 ID", example = "456")
    private Long runningRecordId;

    @Schema(description = "총 이동 거리(미터)", example = "5200")
    private Integer totalDistanceMeters;

    @Schema(description = "총 소요 시간(초)", example = "1800")
    private Integer durationSeconds;

    @Schema(description = "평균 페이스(초/킬로미터)", example = "347")
    private Integer averagePaceSeconds;

    @Schema(description = "칼로리(kcal)", example = "350")
    private Integer calories;

    @Schema(description = "러닝 종료 시각(서버 기준 ISO-8601)", example = "2025-08-10T10:00:00")
    private LocalDateTime endedAt;
}
