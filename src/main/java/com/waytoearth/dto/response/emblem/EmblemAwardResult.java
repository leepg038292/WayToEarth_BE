package com.waytoearth.dto.response.emblem;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@Schema(name = "EmblemAwardResult", description = "엠블럼 지급 결과")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmblemAwardResult {

    @Schema(description = "지급된 엠블럼 개수", example = "2")
    private int awardedCount;

    @Schema(description = "지급된 엠블럼 ID 목록", example = "[1,5]")
    private List<Long> awardedEmblemIds;

    public EmblemAwardResult() { }

    public EmblemAwardResult(int awardedCount, List<Long> awardedEmblemIds) {
        this.awardedCount = awardedCount;
        this.awardedEmblemIds = awardedEmblemIds;
    }

}
