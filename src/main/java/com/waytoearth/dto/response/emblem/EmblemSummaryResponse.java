package com.waytoearth.dto.response.emblem;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@Schema(name = "EmblemSummaryResponse", description = "내 엠블럼 요약")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmblemSummaryResponse {

    @Schema(description = "보유 개수", example = "8")
    private int owned;

    @Schema(description = "전체 개수", example = "15")
    private int total;

    @Schema(description = "완성도(0~1)", example = "0.53")
    private double completionRate;

    public EmblemSummaryResponse() { }

    public EmblemSummaryResponse(int owned, int total, double completionRate) {
        this.owned = owned;
        this.total = total;
        this.completionRate = completionRate;
    }

}
