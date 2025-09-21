package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.dto.response.journey.LandmarkDetailResponse;
import com.waytoearth.dto.response.journey.LandmarkSummaryResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.entity.enums.StoryType;
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

import java.util.List;

@RestController
@RequestMapping("/v1/landmarks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Landmark API", description = "랜드마크 관리 API")
public class LandmarkController {

    private final LandmarkService landmarkService;
    private final FileService fileService;

    @GetMapping("/{landmarkId}")
    @Operation(summary = "랜드마크 상세 정보", description = "랜드마크의 상세 정보를 조회합니다.")
    public ResponseEntity<LandmarkDetailResponse> getLandmark(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId,
            @Parameter(description = "사용자 ID (스탬프 수집 여부 확인용)")
            @RequestParam(required = false) Long userId) {

        LandmarkDetailResponse response = landmarkService.getLandmarkDetail(landmarkId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{landmarkId}/stories")
    @Operation(summary = "랜드마크의 스토리 카드 목록", description = "랜드마크의 모든 스토리 카드를 조회합니다.")
    public ResponseEntity<List<StoryCardResponse>> getStoryCards(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId,
            @Parameter(description = "스토리 타입 필터")
            @RequestParam(required = false) StoryType type) {

        List<StoryCardResponse> storyCards = (type != null)
                ? landmarkService.getStoryCardsByType(landmarkId, type)
                : landmarkService.getStoryCardsByLandmarkId(landmarkId);

        return ResponseEntity.ok(storyCards);
    }

    @GetMapping("/journey/{journeyId}")
    @Operation(summary = "여행의 랜드마크 목록", description = "특정 여행의 모든 랜드마크를 조회합니다.")
    public ResponseEntity<List<LandmarkSummaryResponse>> getLandmarksByJourney(
            @Parameter(description = "여행 ID")
            @PathVariable Long journeyId) {

        List<LandmarkSummaryResponse> landmarks = landmarkService.getLandmarksByJourneyId(journeyId);
        return ResponseEntity.ok(landmarks);
    }

    @Operation(
        summary = "랜드마크 이미지 업로드 Presigned URL 발급",
        description = """
            랜드마크에 사용할 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 10MB

            **S3 저장 경로:**
            - `journeys/landmarks/{yyyy-MM-dd}/{userId}/{uuid}`

            **사용 용도:**
            - 랜드마크 메인 이미지
            - 랜드마크 갤러리 이미지
            - 사용자 제공 랜드마크 사진
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
                      "message": "랜드마크 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/journeys/landmarks/2024-12-21/1/uuid?...",
                        "download_url": "https://bucket.s3.amazonaws.com/journeys/landmarks/2024-12-21/1/uuid?...",
                        "key": "journeys/landmarks/2024-12-21/1/uuid",
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
                          "contentType": "image/png",
                          "size": 10485760
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PresignRequest req
    ) {
        PresignResponse response = fileService.presignLandmark(user.getUserId(), req);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "랜드마크 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }
}