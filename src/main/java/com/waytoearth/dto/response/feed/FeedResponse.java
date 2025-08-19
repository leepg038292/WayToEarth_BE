package com.waytoearth.dto.response.feed;

import com.waytoearth.entity.Feed;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Schema(description = "피드 응답 DTO")
@Builder
public record FeedResponse(
        @Schema(description = "피드 ID", example = "101")
        Long id,

        @Schema(description = "피드 본문(한줄 글쓰기)", example = "오늘 5km 달림!")
        String content,

        @Schema(description = "업로드된 이미지 URL (S3)", example = "https://s3.../feed123.jpg")
        String imageUrl,

        @Schema(description = "좋아요 개수", example = "12")
        int likeCount,

        @Schema(description = "현재 로그인한 사용자가 좋아요 눌렀는지 여부", example = "true")
        boolean liked,   // 👈 새로 추가됨

        @Schema(description = "작성 시간", example = "2025-08-18T02:45:00Z")
        Instant createdAt,

        @Schema(description = "작성자 ID", example = "7")
        Long userId,

        @Schema(description = "작성자 닉네임", example = "평러너")
        String nickname,

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://s3.../profile7.png")
        String profileImageUrl,

        @Schema(description = "연동된 러닝 거리(km)", example = "5.32")
        Double distance,

        @Schema(description = "연동된 러닝 시간(초)", example = "1800")
        Integer duration,

        @Schema(description = "평균 페이스 (분:초/km)", example = "05:45")
        String averagePace,

        @Schema(description = "소모 칼로리", example = "320")
        Integer calories
) {
    public static FeedResponse from(Feed feed, boolean liked) {
        return FeedResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .likeCount(feed.getLikeCount())
                .liked(liked)   // 👈 좋아요 여부 반영
                .createdAt(feed.getCreatedAt())
                .userId(feed.getUser().getId())
                .nickname(feed.getUser().getNickname())
                .profileImageUrl(feed.getUser().getProfileImageUrl())
                .distance(feed.getRunningRecord() != null ?
                        (feed.getRunningRecord().getDistance() != null ?
                                feed.getRunningRecord().getDistance().doubleValue() : null) : null)
                .duration(feed.getRunningRecord() != null ?
                        feed.getRunningRecord().getDuration() : null)
                .averagePace(feed.getRunningRecord() != null ?
                        formatPace(feed.getRunningRecord().getAveragePaceSeconds()) : null)
                .calories(feed.getRunningRecord() != null ?
                        feed.getRunningRecord().getCalories() : null)
                .build();
    }

    private static String formatPace(Integer paceSeconds) {
        if (paceSeconds == null) return null;
        int minutes = paceSeconds / 60;
        int seconds = paceSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
