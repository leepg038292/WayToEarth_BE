package com.waytoearth.dto.response.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(name = "UserSummaryResponse", description = "내 정보 요약")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserSummaryResponse {

    @Schema(description = "완성도(보유 엠블럼/전체)", example = "0.53")
    private double completionRate;

    @Schema(description = "보유 엠블럼 개수", example = "8")
    private int emblemCount;

    @Schema(description = "총 누적 거리(km)", example = "247.8")
    private double totalDistance;

    @Schema(description = "총 러닝 횟수", example = "45")
    private int totalRunningCount;

    public UserSummaryResponse() { }

    public UserSummaryResponse(double completionRate, int emblemCount, double totalDistance, int totalRunningCount) {
        this.completionRate = completionRate;
        this.emblemCount = emblemCount;
        this.totalDistance = totalDistance;
        this.totalRunningCount = totalRunningCount;
    }

}
