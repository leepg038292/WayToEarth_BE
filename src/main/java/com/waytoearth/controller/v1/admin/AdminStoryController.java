package com.waytoearth.controller.v1.admin;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.request.journey.StoryCardCreateRequest;
import com.waytoearth.dto.request.journey.StoryCardUpdateRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.journey.StoryCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/story-cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Story Card API", description = "관리자 전용 스토리 카드 관리 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoryController {

    private final FileService fileService;
    private final StoryCardService storyCardService;

    @Operation(
        summary = "스토리 이미지 업로드 Presigned URL 발급 (관리자 전용)",
        description = """
            관리자가 스토리 카드에 사용할 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **권한:**
            - 관리자만 접근 가능

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 10MB

            **S3 저장 경로:**
            - `journeys/{journeyId}/landmarks/{landmarkId}/stories/{storyId}/{uuid}.jpg`

            **사용 용도:**
            - 스토리 카드 썸네일 이미지
            - 스토리 내용 첨부 이미지 (여러 개 가능)
            - 역사/문화/자연 관련 이미지
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "스토리 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/journeys/1/landmarks/5/stories/10/uuid.jpg?...",
                        "download_url": "https://d1234567890.cloudfront.net/journeys/1/landmarks/5/stories/10/uuid.jpg",
                        "key": "journeys/1/landmarks/5/stories/10/uuid.jpg",
                        "expires_in": 300
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 형식 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한 없음 (관리자 권한 필요)"
        )
    })
    @PostMapping("/{journeyId}/{landmarkId}/{storyId}/image/presign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "여정 ID") @PathVariable Long journeyId,
            @io.swagger.v3.oas.annotations.Parameter(description = "랜드마크 ID") @PathVariable Long landmarkId,
            @io.swagger.v3.oas.annotations.Parameter(description = "스토리 ID") @PathVariable Long storyId,
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
        log.info("관리자 스토리 이미지 업로드 URL 발급 요청: userId={}, journeyId={}, landmarkId={}, storyId={}, contentType={}, size={}",
                user.getUserId(), journeyId, landmarkId, storyId, req.getContentType(), req.getSize());

        PresignResponse response = fileService.presignStory(journeyId, landmarkId, storyId, req);

        return ResponseEntity.ok(ApiResponse.success(response, "스토리 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }

    @Operation(
        summary = "스토리 카드 생성 (관리자 전용)",
        description = """
            관리자가 특정 랜드마크에 새로운 스토리 카드를 생성합니다.

            **권한:**
            - 관리자만 접근 가능

            **스토리 타입:**
            - HISTORY: 역사 관련 스토리
            - CULTURE: 문화 관련 스토리
            - NATURE: 자연 관련 스토리

            **순서 관리:**
            - orderIndex로 스토리 표시 순서 제어
            - 0부터 시작하며, 작은 숫자가 먼저 표시됨
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "스토리 카드 생성 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 값 누락, 유효성 검증 실패)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "랜드마크를 찾을 수 없음"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<StoryCardResponse>> createStoryCard(
            @AuthUser AuthenticatedUser user,
            @Valid @org.springframework.web.bind.annotation.RequestBody StoryCardCreateRequest request
    ) {
        log.info("관리자 스토리 카드 생성 요청: userId={}, landmarkId={}, title={}",
                user.getUserId(), request.landmarkId(), request.title());

        StoryCardResponse response = storyCardService.createStoryCard(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "스토리 카드가 성공적으로 생성되었습니다."));
    }

    @Operation(
        summary = "스토리 카드 수정 (관리자 전용)",
        description = """
            관리자가 기존 스토리 카드의 정보를 수정합니다.

            **권한:**
            - 관리자만 접근 가능

            **수정 가능 항목:**
            - 제목, 내용, 이미지 URL, 타입, 순서
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "스토리 카드 수정 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "스토리 카드를 찾을 수 없음"
        )
    })
    @PutMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryCardResponse>> updateStoryCard(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "스토리 카드 ID") @PathVariable Long storyId,
            @Valid @org.springframework.web.bind.annotation.RequestBody StoryCardUpdateRequest request
    ) {
        log.info("관리자 스토리 카드 수정 요청: userId={}, storyId={}, title={}",
                user.getUserId(), storyId, request.title());

        StoryCardResponse response = storyCardService.updateStoryCard(storyId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "스토리 카드가 성공적으로 수정되었습니다."));
    }

    @Operation(
        summary = "스토리 카드 삭제 (관리자 전용)",
        description = """
            관리자가 스토리 카드를 삭제합니다.

            **권한:**
            - 관리자만 접근 가능

            **주의:**
            - 삭제된 스토리 카드는 복구할 수 없습니다
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "스토리 카드 삭제 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "스토리 카드를 찾을 수 없음"
        )
    })
    @DeleteMapping("/{storyId}")
    public ResponseEntity<ApiResponse<Void>> deleteStoryCard(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "스토리 카드 ID") @PathVariable Long storyId
    ) {
        log.info("관리자 스토리 카드 삭제 요청: userId={}, storyId={}", user.getUserId(), storyId);

        storyCardService.deleteStoryCard(storyId);

        return ResponseEntity.ok(ApiResponse.success(null, "스토리 카드가 성공적으로 삭제되었습니다."));
    }
}