// com/waytoearth/controller/v1/FileController.java
package com.waytoearth.controller.v1.file;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.user.UserService;
import com.waytoearth.service.crew.CrewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "파일 업로드", description = "S3 Presigned URL 기반 파일 업로드 API")
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserService userService;
    private final CrewService crewService;

    @Operation(
        summary = "프로필 이미지 업로드 Presigned URL 발급",
        description = """
            사용자 프로필 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 5MB

            **S3 저장 경로:**
            - `profiles/{userId}/profile.{extension}`
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
                      "message": "프로필 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/profiles/1/profile.jpg?...",
                        "download_url": "https://bucket.s3.amazonaws.com/profiles/1/profile.jpg?...",
                        "key": "profiles/1/profile.jpg",
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
    @PostMapping("/presign/profile")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<PresignResponse>> presignProfile(
            @AuthUser AuthenticatedUser me,
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
                          "size": 2048000
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PresignRequest request
    ) {
        PresignResponse response = fileService.presignProfile(me.getUserId(), request);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "프로필 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }

    @Operation(
        summary = "피드 이미지 업로드 Presigned URL 발급",
        description = """
            피드 게시물에 첨부할 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 10MB

            **S3 저장 경로:**
            - `feeds/{yyyy-MM-dd}/{userId}/{uuid}`
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
                      "message": "피드 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/feeds/2024-12-21/1/uuid?...",
                        "download_url": "https://bucket.s3.amazonaws.com/feeds/2024-12-21/1/uuid?...",
                        "key": "feeds/2024-12-21/1/uuid",
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
    @PostMapping("/presign/feed")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<PresignResponse>> presignFeed(
            @AuthUser AuthenticatedUser me,
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
                          "size": 5120000
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PresignRequest request
    ) {
        PresignResponse response = fileService.presignFeed(me.getUserId(), request);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "피드 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }


    @Operation(
        summary = "프로필 이미지 삭제",
        description = """
            사용자의 프로필 이미지를 S3에서 삭제하고 데이터베이스 정보를 초기화합니다.

            **동작:**
            - S3에서 기존 프로필 이미지 파일 삭제
            - 사용자 프로필 이미지 URL 및 Key 필드 초기화
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 이미지 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "프로필 이미지가 성공적으로 삭제되었습니다.",
                      "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<Void>> deleteProfileImage(@AuthUser AuthenticatedUser me) {
        userService.removeProfileImage(me.getUserId());
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success("프로필 이미지가 성공적으로 삭제되었습니다."));
    }

    @Operation(
        summary = "크루 프로필 이미지 업로드 Presigned URL 발급",
        description = """
            크루 프로필 이미지를 업로드하기 위한 S3 Presigned URL을 발급합니다.

            **지원 파일 형식:**
            - JPEG (.jpg, .jpeg)
            - PNG (.png)
            - WebP (.webp)

            **파일 크기 제한:**
            - 최대 5MB

            **S3 저장 경로:**
            - `crews/{crewId}/profile.{extension}`

            **권한:**
            - 크루장만 업로드 가능
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
                      "message": "크루 프로필 이미지 업로드 URL이 성공적으로 발급되었습니다.",
                      "data": {
                        "upload_url": "https://bucket.s3.amazonaws.com/crews/123/profile.jpg?...",
                        "download_url": "https://bucket.s3.amazonaws.com/crews/123/profile.jpg?...",
                        "key": "crews/123/profile.jpg",
                        "expires_in": 300
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 형식 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @PostMapping("/presign/crew/{crewId}")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<PresignResponse>> presignCrewProfile(
            @AuthUser AuthenticatedUser me,
            @io.swagger.v3.oas.annotations.Parameter(description = "크루 ID") @PathVariable Long crewId,
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
                          "size": 2048000
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PresignRequest request
    ) {
        PresignResponse response = fileService.presignCrewProfile(me.getUserId(), crewId, request);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success(response, "크루 프로필 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }

    @Operation(
        summary = "크루 프로필 이미지 삭제",
        description = """
            크루의 프로필 이미지를 S3에서 삭제하고 데이터베이스 정보를 초기화합니다.

            **동작:**
            - S3에서 기존 크루 프로필 이미지 파일 삭제
            - 크루 프로필 이미지 URL 필드 초기화

            **권한:**
            - 크루장만 삭제 가능
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "크루 프로필 이미지 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "success": true,
                      "message": "크루 프로필 이미지가 성공적으로 삭제되었습니다.",
                      "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @DeleteMapping("/crew/{crewId}/profile")
    public ResponseEntity<com.waytoearth.dto.response.common.ApiResponse<Void>> deleteCrewProfileImage(
            @AuthUser AuthenticatedUser me,
            @io.swagger.v3.oas.annotations.Parameter(description = "크루 ID") @PathVariable Long crewId) {
        crewService.removeCrewProfileImage(me, crewId);
        return ResponseEntity.ok(com.waytoearth.dto.response.common.ApiResponse.success("크루 프로필 이미지가 성공적으로 삭제되었습니다."));
    }
}
