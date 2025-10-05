package com.waytoearth.dto.response.running.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningAnalysisResponse {

    /** 피드백 ID */
    private Long feedbackId;

    /** 러닝 기록 ID */
    private Long runningRecordId;

    /** AI 분석 피드백 내용 */
    private String feedbackContent;

    /** 분석 생성 시각 */
    private LocalDateTime createdAt;

    /** 사용된 모델명 */
    private String modelName;
}
