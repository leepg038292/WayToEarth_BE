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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            - 스토리 타입 (HISTORY/CULTURE/NATURE)
            - 표시 순서

            **스토리 타입:**
            - HISTORY: 역사 정보
            - CULTURE: 문화 정보
            - NATURE: 자연 정보
            """,
        tags = {"Story Card API"}
    )
    public ResponseEntity<StoryCardResponse> getStoryCard(
            @Parameter(description = "스토리 카드 ID")
            @PathVariable Long storyCardId) {

        StoryCardResponse response = landmarkService.getStoryCardById(storyCardId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "스토리 이미지 업로드 Presigned URL 발급",
        description = """
            스토리 카드에 사용할 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 10MB

            **S3 저장 경로:**
            - `journeys/stories/{yyyy-MM-dd}/{userId}/{uuid}`

            **사용 용도:**
            - 스토리 카드 썸네일 이미지
            - 스토리 내용 첨부 이미지
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.waytoearth.dto.response.common.ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "스토리 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/journeys/stories/2024-12-21/1/uuid?...",
                        "download_url": "https://bucket.s3.amazonaws.com/journeys/stories/2024-12-21/1/uuid?...",
                        "key": "journeys/stories/2024-12-21/1/uuid",
                        "expires_in": 300
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 형식 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/image/presign")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @RequestBody(
                description = "업로드할 파일 정보",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PresignRequest.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "contentType": "image/jpeg",
                          "size": 6291456
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PresignRequest req
    ) {
        PresignResponse response = fileService.presignStory(user.getUserId(), req);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "스토리 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }
}