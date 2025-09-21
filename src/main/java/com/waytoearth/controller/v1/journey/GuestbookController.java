package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.request.journey.GuestbookCreateRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.dto.response.journey.GuestbookResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.journey.GuestbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/guestbook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guestbook API", description = "방명록 관리 API")
public class GuestbookController {

    private final GuestbookService guestbookService;
    private final FileService fileService;

    @PostMapping
    @Operation(
        summary = "방명록 작성",
        description = """
            랜드마크에 방명록을 작성합니다.

            **작성 가능 항목:**
            - 메시지 (필수, 최대 500자)
            - 사진 업로드 (선택)
            - 기분 표현 (HAPPY, EXCITED, TIRED, AMAZED)
            - 평점 (1-5점)
            - 공개/비공개 설정

            **기분 유형:**
            - HAPPY: 행복한
            - EXCITED: 신난
            - TIRED: 피곤한
            - AMAZED: 놀란
            """,
        tags = {"Guestbook API"}
    )
    public ResponseEntity<GuestbookResponse> createGuestbook(
            @Parameter(description = "사용자 ID")
            @RequestParam Long userId,
            @Valid @RequestBody GuestbookCreateRequest request) {

        GuestbookResponse response = guestbookService.createGuestbook(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/landmarks/{landmarkId}")
    @Operation(summary = "랜드마크별 방명록 조회", description = "특정 랜드마크의 공개 방명록을 조회합니다.")
    public ResponseEntity<Page<GuestbookResponse>> getLandmarkGuestbook(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<GuestbookResponse> guestbooks = guestbookService.getGuestbookByLandmark(landmarkId, pageable);

        return ResponseEntity.ok(guestbooks);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "내 방명록 목록", description = "사용자가 작성한 모든 방명록을 조회합니다.")
    public ResponseEntity<List<GuestbookResponse>> getUserGuestbook(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId) {

        List<GuestbookResponse> guestbooks = guestbookService.getUserGuestbook(userId);
        return ResponseEntity.ok(guestbooks);
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 방명록 조회", description = "전체 공개 방명록을 최신순으로 조회합니다.")
    public ResponseEntity<Page<GuestbookResponse>> getRecentGuestbook(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<GuestbookResponse> guestbooks = guestbookService.getRecentGuestbook(pageable);
        return ResponseEntity.ok(guestbooks);
    }

    @GetMapping("/landmarks/{landmarkId}/statistics")
    @Operation(summary = "랜드마크 통계", description = "랜드마크의 방명록 및 방문자 통계를 조회합니다.")
    public ResponseEntity<GuestbookService.LandmarkStatistics> getLandmarkStatistics(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId) {

        GuestbookService.LandmarkStatistics statistics = guestbookService.getLandmarkStatistics(landmarkId);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/image/presign")
    @Operation(summary = "방명록 이미지 업로드 Presigned URL 발급", description = "방명록에 첨부할 이미지를 업로드할 수 있도록 S3 Presigned URL을 발급합니다.")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody PresignRequest req
    ) {
        PresignResponse response = fileService.presignGuestbook(user.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(response, "방명록 이미지 업로드 URL이 성공적으로 발급되었습니다."));
    }
}