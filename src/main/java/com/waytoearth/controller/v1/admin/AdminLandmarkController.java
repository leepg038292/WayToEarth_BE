package com.waytoearth.controller.v1.admin;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.request.journey.LandmarkImageUpdateRequest;
import com.waytoearth.dto.request.journey.GalleryImageCreateRequest;
import com.waytoearth.dto.request.journey.GalleryImagesReorderRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.journey.LandmarkService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/landmarks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Landmark API", description = "관리자 전용 랜드마크 관리 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLandmarkController {

    private final FileService fileService;
    private final LandmarkService landmarkService;

    @Operation(
        summary = "랜드마크 이미지 업로드 Presigned URL 발급 (관리자 전용)",
        description = """
            관리자가 랜드마크에 사용할 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **권한:**
            - 관리자만 접근 가능

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 10MB

            **S3 저장 경로:**
            - `journeys/{journeyId}/landmarks/{landmarkId}/{uuid}.jpg`

            **사용 용도:**
            - 랜드마크 메인 이미지
            - 랜드마크 갤러리 이미지 (여러 개 가능)
            - 랜드마크 배경 이미지
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
                      "message": "랜드마크 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/journeys/1/landmarks/5/uuid.jpg?...",
                        "download_url": "https://d1234567890.cloudfront.net/journeys/1/landmarks/5/uuid.jpg",
                        "key": "journeys/1/landmarks/5/uuid.jpg",
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
    @PostMapping("/{journeyId}/{landmarkId}/image/presign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "여정 ID") @PathVariable Long journeyId,
            @io.swagger.v3.oas.annotations.Parameter(description = "랜드마크 ID") @PathVariable Long landmarkId,
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
        log.info("관리자 랜드마크 이미지 업로드 URL 발급 요청: userId={}, journeyId={}, landmarkId={}, contentType={}, size={}",
                user.getUserId(), journeyId, landmarkId, req.getContentType(), req.getSize());

        PresignResponse response = fileService.presignLandmark(journeyId, landmarkId, req);

        return ResponseEntity.ok(ApiResponse.success(response, "랜드마크 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }

    @Deprecated
    @Operation(
        summary = "랜드마크 대표(커버) 이미지 URL 업데이트 (관리자 전용)",
        description = """
            랜드마크의 대표(커버) 이미지를 업데이트합니다. 갤러리 이미지는 별도 API를 사용하세요.

            - 갤러리 이미지 추가: POST /v1/admin/landmarks/{landmarkId}/images
            - 갤러리 이미지 삭제: DELETE /v1/admin/landmarks/images/{imageId}
            - 갤러리 이미지 정렬: PATCH /v1/admin/landmarks/{landmarkId}/images/reorder
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이미지 URL 업데이트 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "랜드마크를 찾을 수 없음"
        )
    })
    @PutMapping("/{landmarkId}")
    public ResponseEntity<ApiResponse<Void>> updateLandmarkImage(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "랜드마크 ID") @PathVariable Long landmarkId,
            @Valid @org.springframework.web.bind.annotation.RequestBody LandmarkImageUpdateRequest request
    ) {
        log.info("관리자 랜드마크 이미지 업데이트: userId={}, landmarkId={}, imageUrl={}",
                user.getUserId(), landmarkId, request.imageUrl());

        landmarkService.updateLandmarkImage(landmarkId, request.imageUrl());

        return ResponseEntity.ok(ApiResponse.success(null, "랜드마크 이미지가 성공적으로 업데이트되었습니다."));
    }

    @Operation(
        summary = "랜드마크 갤러리 이미지 추가 (관리자 전용)",
        description = "랜드마크에 갤러리 이미지를 추가합니다. orderIndex는 자동 부여됩니다."
    )
    @PostMapping("/{landmarkId}/images")
    public ResponseEntity<ApiResponse<Void>> addLandmarkImage(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "랜드마크 ID") @PathVariable Long landmarkId,
            @Valid @org.springframework.web.bind.annotation.RequestBody GalleryImageCreateRequest request
    ) {
        landmarkService.addLandmarkImage(landmarkId, request.imageUrl());
        return ResponseEntity.ok(ApiResponse.success(null, "갤러리 이미지가 추가되었습니다."));
    }

    @Operation(
        summary = "랜드마크 갤러리 이미지 삭제 (관리자 전용)",
        description = "갤러리 이미지 단건을 삭제합니다."
    )
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteLandmarkImage(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "이미지 ID") @PathVariable Long imageId
    ) {
        landmarkService.deleteLandmarkImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "갤러리 이미지가 삭제되었습니다."));
    }

    @Operation(
        summary = "랜드마크 갤러리 이미지 순서 변경 (관리자 전용)",
        description = "imageIds를 원하는 순서로 전달하면 orderIndex를 재정렬합니다."
    )
    @PatchMapping("/{landmarkId}/images/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderLandmarkImages(
            @AuthUser AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.Parameter(description = "랜드마크 ID") @PathVariable Long landmarkId,
            @Valid @org.springframework.web.bind.annotation.RequestBody GalleryImagesReorderRequest request
    ) {
        landmarkService.reorderLandmarkImages(landmarkId, request.imageIds());
        return ResponseEntity.ok(ApiResponse.success(null, "갤러리 이미지 순서를 변경했습니다."));
    }
}
