package com.waytoearth.dto.response.emblem;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Builder
@Schema(name = "EmblemCatalogItem", description = "엠블럼 카탈로그 항목")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmblemCatalogItem {

    @Schema(description = "엠블럼 ID", example = "12")
    private Long emblemId;

    @Schema(description = "이름", example = "첫 러닝 완주")
    private String name;

    @Schema(description = "설명", example = "첫 러닝을 완주하면 지급됩니다.")
    private String description;

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/emblems/12.png")
    private String imageUrl;

    @Schema(description = "희귀도", example = "COMMON")
    private String rarity;

    @Schema(description = "보유 여부", example = "true")
    private boolean owned;

    @Schema(description = "획득 시각(보유 시)", example = "2025-01-26T10:00:00Z")
    private Instant earnedAt;

    public EmblemCatalogItem() { }

    public EmblemCatalogItem(Long emblemId, String name, String description, String imageUrl,
                             String rarity, boolean owned, Instant earnedAt) {
        this.emblemId = emblemId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
        this.owned = owned;
        this.earnedAt = earnedAt;
    }

}
