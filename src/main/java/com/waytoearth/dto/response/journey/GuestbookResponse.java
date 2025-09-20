package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.Journey.GuestbookEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "방명록 응답")
public record GuestbookResponse(
    @Schema(description = "방명록 ID", example = "1")
    Long id,

    @Schema(description = "작성자 정보")
    UserSummaryResponse user,

    @Schema(description = "랜드마크 정보")
    LandmarkSummaryResponse landmark,

    @Schema(description = "방명록 메시지", example = "정말 아름다운 곳이에요!")
    String message,

    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    String photoUrl,

    @Schema(description = "기분", example = "AMAZED")
    String mood,

    @Schema(description = "평점 (1-5)", example = "5")
    Integer rating,

    @Schema(description = "작성 시간", example = "2024-01-15T14:30:00")
    LocalDateTime createdAt
) {
    public static GuestbookResponse from(GuestbookEntity guestbook) {
        return new GuestbookResponse(
            guestbook.getId(),
            UserSummaryResponse.from(guestbook.getUser()),
            LandmarkSummaryResponse.from(guestbook.getLandmark()),
            guestbook.getMessage(),
            guestbook.getPhotoUrl(),
            guestbook.getMood() != null ? guestbook.getMood().name() : null,
            guestbook.getRating(),
            guestbook.getCreatedAt()
        );
    }
}