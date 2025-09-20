package com.waytoearth.dto.Journey.request;

import com.waytoearth.entity.Journey.GuestbookEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "방명록 작성 요청")
public record GuestbookCreateRequest(
    @NotNull(message = "랜드마크 ID는 필수입니다")
    @Schema(description = "랜드마크 ID", example = "1")
    Long landmarkId,

    @NotBlank(message = "메시지는 필수입니다")
    @Size(max = 500, message = "메시지는 500자 이하여야 합니다")
    @Schema(description = "방명록 메시지", example = "정말 아름다운 곳이에요!")
    String message,

    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    String photoUrl,

    @Schema(description = "기분", example = "AMAZED")
    GuestbookEntity.Mood mood,

    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하여야 합니다")
    @Schema(description = "평점 (1-5)", example = "5")
    Integer rating,

    @Schema(description = "공개 여부", example = "true")
    Boolean isPublic
) {}