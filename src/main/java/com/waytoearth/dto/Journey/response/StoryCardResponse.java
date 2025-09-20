package com.waytoearth.dto.Journey.response;

import com.waytoearth.entity.Journey.StoryCardEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 카드 응답")
public record StoryCardResponse(
    @Schema(description = "스토리 카드 ID", example = "1")
    Long id,

    @Schema(description = "스토리 제목", example = "경복궁의 역사")
    String title,

    @Schema(description = "스토리 내용", example = "경복궁은 1395년 태조 이성계에 의해 창건된...")
    String content,

    @Schema(description = "스토리 이미지 URL", example = "https://example.com/story.jpg")
    String imageUrl,

    @Schema(description = "오디오 URL", example = "https://example.com/audio.mp3")
    String audioUrl,

    @Schema(description = "스토리 타입", example = "HISTORY")
    String type,

    @Schema(description = "표시 순서", example = "1")
    Integer orderIndex
) {
    public static StoryCardResponse from(StoryCardEntity storyCard) {
        return new StoryCardResponse(
            storyCard.getId(),
            storyCard.getTitle(),
            storyCard.getContent(),
            storyCard.getImageUrl(),
            storyCard.getAudioUrl(),
            storyCard.getType().name(),
            storyCard.getOrderIndex()
        );
    }
}