package com.waytoearth.dto.response.journey;

import com.waytoearth.entity.journey.StoryCardEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "스토리 카드 응답")
public record StoryCardResponse(
    @Schema(description = "스토리 카드 ID", example = "1")
    Long id,

    @Schema(description = "스토리 제목", example = "경복궁의 역사")
    String title,

    @Schema(description = "스토리 내용", example = "경복궁은 1395년 태조 이성계에 의해 창건된...")
    String content,

    @Schema(description = "대표 이미지 URL (하위호환)", example = "https://example.com/story.jpg")
    String imageUrl,

    @Schema(description = "갤러리 이미지 목록")
    List<String> images,

    @Schema(description = "스토리 타입", example = "HISTORY")
    String type,

    @Schema(description = "표시 순서", example = "1")
    Integer orderIndex
) {
    public static StoryCardResponse from(StoryCardEntity storyCard) {
        List<String> imageUrls = storyCard.getImages() == null ? List.of()
                : storyCard.getImages().stream()
                .sorted(java.util.Comparator.comparingInt(i -> i.getOrderIndex() == null ? 0 : i.getOrderIndex()))
                .map(img -> img.getImageUrl())
                .toList();

        return new StoryCardResponse(
            storyCard.getId(),
            storyCard.getTitle(),
            storyCard.getContent(),
            storyCard.getImageUrl(),
            imageUrls,
            storyCard.getType().name(),
            storyCard.getOrderIndex()
        );
    }
}
