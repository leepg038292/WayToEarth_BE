package com.waytoearth.dto.response.running;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "RunningCompleteResponse", description = "러닝 완료 응답 DTO")
@Getter
@AllArgsConstructor
public class RunningCompleteResponse {

    @Schema(description = "러닝 기록 ID", example = "123")
    private Long runningRecordId;

    @Schema(description = "이동 거리(km)", example = "5.21")
    private BigDecimal distance;

    @Schema(description = "소요 시간(초)", example = "1827")
    private Integer duration;

    @Schema(description = "평균 페이스(분:초/km)", example = "05:51")
    private String averagePace;

    @Schema(description = "칼로리(kcal)", example = "312")
    private Integer calories;

    @Schema(description = "종료 시각")
    private LocalDateTime endedAt;
}
