// com/waytoearth/controller/v1/FileController.java
package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 업로드", description = "S3 Presigned URL 기반 파일 업로드 API")
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "프로필 이미지 업로드 Presigned URL 발급", description = "사용자가 프로필 이미지를 업로드할 수 있도록 S3 Presigned URL을 발급한다.")
    @PostMapping("/presign/profile")
    public ResponseEntity<PresignResponse> presignProfile(
            @AuthUser AuthenticatedUser me,
            @Valid @RequestBody PresignRequest request
    ) {
        return ResponseEntity.ok(fileService.presignProfile(me.getUserId(), request));
    }

    @Operation(summary = "피드 이미지 업로드 Presigned URL 발급", description = "사용자가 피드 공유 시 이미지를 업로드할 수 있도록 S3 Presigned URL을 발급한다.")
    @PostMapping("/presign/feed")
    public ResponseEntity<PresignResponse> presignFeed(
            @AuthUser AuthenticatedUser me,
            @Valid @RequestBody PresignRequest request
    ) {
        return ResponseEntity.ok(fileService.presignFeed(me.getUserId(), request));
    }
}
