package com.waytoearth.dto.request.feed;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FeedCreateRequest {

    @Schema(description = "연결된 러닝 기록 ID", example = "456")
    private Long runningRecordId;

    @Schema(description = "피드 텍스트", example = "오늘 5km 달렸어요! 🏃‍♂️")
    private String content;

    @Schema(description = "이미지 URL (S3 업로드 후 경로)", example = "https://example.com/running_photo.jpg")
    private String imageUrl;

    @Schema(description = "이미지 Key (S3 삭제 시 필요)", example = "feeds/2025-08-24/1/uuid1234")
    private String imageKey; //  추가
}
