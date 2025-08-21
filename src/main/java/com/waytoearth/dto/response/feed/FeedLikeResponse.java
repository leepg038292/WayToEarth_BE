package com.waytoearth.dto.response.feed;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 응답 DTO")
public record FeedLikeResponse(
        @Schema(description = "피드 ID") Long feedId,
        @Schema(description = "현재 좋아요 수") int likeCount,
        @Schema(description = "좋아요 여부") boolean liked
) {}
