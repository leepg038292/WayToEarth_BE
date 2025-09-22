package com.waytoearth.controller.v1.admin;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
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
            - `journeys/landmarks/{yyyy-MM-dd}/{userId}/{uuid}`

            **사용 용도:**
            - 랜드마크 메인 이미지
            - 랜드마크 갤러리 이미지
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
    @PostMapping("/image/presign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
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
        log.info("관리자 랜드마크 이미지 업로드 URL 발급 요청: userId={}, contentType={}, size={}",
                user.getUserId(), req.getContentType(), req.getSize());

        PresignResponse response = fileService.presignLandmark(user.getUserId(), req);

        return ResponseEntity.ok(ApiResponse.success(response, "랜드마크 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }
}