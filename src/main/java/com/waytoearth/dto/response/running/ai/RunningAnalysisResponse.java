package com.waytoearth.dto.response.running.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningAnalysisResponse {

    /** 피드백 ID */
    @Schema(description = "피드백 ID", example = "123")
    private Long feedbackId;

    /** 러닝 기록 ID */
    @Schema(description = "러닝 기록 ID", example = "456")
    private Long runningRecordId;

    /** AI 분석 피드백 내용 */
    @Schema(description = "AI 분석 피드백 내용")
    private String feedbackContent;

    /** 분석 생성 시각 (UTC) */
    @Schema(description = "분석 생성 시각 (UTC)", example = "2025-11-09T10:17:09Z")
    private Instant createdAt;

    /** 사용된 모델명 */
    @Schema(description = "사용된 모델명", example = "gpt-4o-mini")
    private String modelName;
}
