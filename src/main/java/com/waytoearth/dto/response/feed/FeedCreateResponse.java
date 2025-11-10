package com.waytoearth.dto.response.feed;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FeedCreateResponse {

    @Schema(description = "생성된 피드 ID", example = "123")
    private Long feedId;

    @Schema(description = "작성일시 (UTC)", example = "2025-01-26T02:05:00Z")
    private Instant createdAt;
}
