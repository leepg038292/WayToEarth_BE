package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.journey.StampCollectRequest;
import com.waytoearth.dto.response.journey.StampResponse;
import com.waytoearth.service.journey.StampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/stamps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stamp API", description = "스탬프 수집 API")
public class StampController {

    private final StampService stampService;

    @PostMapping("/collect")
    @Operation(
        summary = "스탬프 수집",
        description = """
            랜드마크에서 스탬프를 수집합니다.

            **수집 조건:**
            - 랜드마크 500m 반경 내에 위치해야 함
            - 해당 랜드마크에 진행률상 도달해야 함
            - 중복 수집 불가

            **스탬프 등급:**
            - BRONZE: 일반 스탬프
            - SILVER: 희귀 스탬프
            - GOLD: 에픽 스탬프
            - DIAMOND: 전설 스탬프

            **특별 스탬프 조건:**
            - 여정의 첫 번째/마지막 랜드마크
            - 사용자의 첫 번째 스탬프 수집
            """,
        tags = {"Stamp API"}
    )
    public ResponseEntity<StampResponse> collectStamp(@Valid @RequestBody StampCollectRequest request) {
        StampResponse response = stampService.collectStamp(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "사용자 스탬프 목록", description = "사용자가 수집한 모든 스탬프를 조회합니다.")
    public ResponseEntity<List<StampResponse>> getUserStamps(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId) {

        List<StampResponse> stamps = stampService.getStampsByUserId(userId);
        return ResponseEntity.ok(stamps);
    }

    @GetMapping("/progress/{progressId}")
    @Operation(summary = "여행별 스탬프 목록", description = "특정 여행에서 수집한 스탬프를 조회합니다.")
    public ResponseEntity<List<StampResponse>> getProgressStamps(
            @Parameter(description = "여행 진행 ID")
            @PathVariable Long progressId) {

        List<StampResponse> stamps = stampService.getStampsByProgressId(progressId);
        return ResponseEntity.ok(stamps);
    }


    @GetMapping("/users/{userId}/statistics")
    @Operation(summary = "스탬프 통계", description = "사용자의 스탬프 수집 통계를 조회합니다.")
    public ResponseEntity<StampService.StampStatistics> getStampStatistics(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId) {

        StampService.StampStatistics statistics = stampService.getStampStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/check-collection")
    @Operation(summary = "스탬프 수집 가능 여부 확인", description = "현재 위치에서 스탬프 수집이 가능한지 확인합니다.")
    public ResponseEntity<Boolean> checkCollectionAvailability(
            @Parameter(description = "여행 진행 ID")
            @RequestParam Long progressId,
            @Parameter(description = "랜드마크 ID")
            @RequestParam Long landmarkId,
            @Parameter(description = "현재 위도")
            @RequestParam Double latitude,
            @Parameter(description = "현재 경도")
            @RequestParam Double longitude) {

        boolean canCollect = stampService.canCollectStamp(progressId, landmarkId, latitude, longitude);
        return ResponseEntity.ok(canCollect);
    }
}