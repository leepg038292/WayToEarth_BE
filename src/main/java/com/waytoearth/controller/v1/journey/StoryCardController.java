package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.journey.LandmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/story-cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Story Card API", description = "스토리 카드 API")
public class StoryCardController {

    private final LandmarkService landmarkService;
    private final FileService fileService;

    @GetMapping("/{storyCardId}")
    @Operation(
        summary = "스토리 카드 상세",
        description = """
            스토리 카드의 상세 정보를 조회합니다.

            **조회 정보:**
            - 스토리 제목 및 내용
            - 스토리 이미지
            - 오디오 가이드 URL (있는 경우)
            - 스토리 타입 (HISTORY/CULTURE/NATURE/LOCAL_TIP)
            - 표시 순서

            **스토리 타입:**
            - HISTORY: 역사 정보
            - CULTURE: 문화 정보
            - NATURE: 자연 정보
            - LOCAL_TIP: 현지 팁
            """,
        tags = {"Story Card API"}
    )
    public ResponseEntity<StoryCardResponse> getStoryCard(
            @Parameter(description = "스토리 카드 ID")
            @PathVariable Long storyCardId) {

        StoryCardResponse response = landmarkService.getStoryCardById(storyCardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image/presign")
    @Operation(summary = "스토리 이미지 업로드 Presigned URL 발급", description = "스토리 카드에 사용할 이미지를 업로드할 수 있도록 S3 Presigned URL을 발급합니다.")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody PresignRequest req
    ) {
        PresignResponse response = fileService.presignStory(user.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(response, "스토리 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }
}