package com.waytoearth.dto.request.journey;

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

    @Schema(description = "공개 여부", example = "true")
    Boolean isPublic
) {}