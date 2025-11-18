package com.waytoearth.dto.response.running;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "페이스 코치 체크 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaceCoachCheckResponse {

    @Schema(description = "페이스 코치 활성화 여부", example = "true")
    @JsonProperty("coach_enabled")
    private Boolean coachEnabled;

    @Schema(description = "사용 가능 여부 (5회 이상)", example = "true")
    @JsonProperty("is_available")
    private Boolean isAvailable;

    @Schema(description = "알림을 띄워야 하는지 (느릴 때만 true)", example = "true")
    @JsonProperty("should_alert")
    private Boolean shouldAlert;

    @Schema(description = "기준 평균 페이스 (초/km)", example = "360")
    @JsonProperty("reference_pace_seconds")
    private Integer referencePaceSeconds;

    @Schema(description = "기준 평균 페이스 (포맷)", example = "6:00/km")
    @JsonProperty("reference_pace_formatted")
    private String referencePaceFormatted;

    @Schema(description = "현재 페이스 (초/km)", example = "380")
    @JsonProperty("current_pace_seconds")
    private Integer currentPaceSeconds;

    @Schema(description = "현재 페이스 (포맷)", example = "6:20/km")
    @JsonProperty("current_pace_formatted")
    private String currentPaceFormatted;

    @Schema(description = "페이스 차이 (초)", example = "20")
    @JsonProperty("difference_seconds")
    private Integer differenceSeconds;

    @Schema(description = "알림 메시지", example = "평균보다 20초 느려요! 조금만 더 힘내세요!")
    @JsonProperty("alert_message")
    private String alertMessage;

    @Schema(description = "최소 필요 러닝 기록 수", example = "5")
    @JsonProperty("minimum_records_required")
    private Integer minimumRecordsRequired;

    @Schema(description = "현재 러닝 기록 수", example = "3")
    @JsonProperty("current_records")
    private Integer currentRecords;

    @Schema(description = "안내 메시지", example = "페이스 코치는 5회 이상 러닝 후 사용 가능합니다")
    @JsonProperty("message")
    private String message;
}
