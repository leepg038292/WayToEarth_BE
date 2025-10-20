package com.waytoearth.dto.request.journey;

import com.waytoearth.entity.enums.StoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "스토리 카드 수정 요청")
public record StoryCardUpdateRequest(
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    @Schema(description = "스토리 제목", example = "에펠탑의 역사")
    String title,

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
    @Schema(description = "스토리 내용", example = "에펠탑은 1889년 파리 만국박람회를 기념하여 건설되었습니다...")
    String content,

    @Schema(description = "이미지 URL", example = "https://cdn.waytoearth.com/stories/eiffel-tower.jpg")
    String imageUrl,

    @NotNull(message = "스토리 타입은 필수입니다")
    @Schema(description = "스토리 타입", example = "HISTORY", allowableValues = {"HISTORY", "CULTURE", "NATURE"})
    StoryType type,

    @NotNull(message = "정렬 순서는 필수입니다")
    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    @Schema(description = "정렬 순서 (0부터 시작)", example = "0")
    Integer orderIndex
) {}
