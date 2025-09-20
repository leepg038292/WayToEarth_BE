package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.journey.JourneyProgressUpdateRequest;
import com.waytoearth.dto.response.journey.JourneyProgressResponse;
import com.waytoearth.service.journey.JourneyService;
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
@RequestMapping("/v1/journey-progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journey Progress API", description = "여행 진행률 관리 API")
public class JourneyProgressController {

    private final JourneyService journeyService;

    @PutMapping("/{progressId}")
    @Operation(
        summary = "진행률 업데이트",
        description = """
            여행 진행률을 업데이트합니다.

            **업데이트 정보:**
            - 이번 세션에서 뛴 거리
            - 현재 위치 정보
            - 운동 시간 및 칼로리
            - 평균 페이스

            **자동 처리:**
            - 총 누적 거리 계산
            - 진행률 퍼센티지 계산
            - 다음 랜드마크 정보 업데이트
            - 100% 달성 시 자동 완료 처리
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<JourneyProgressResponse> updateProgress(
            @Parameter(description = "진행 ID")
            @PathVariable Long progressId,
            @Valid @RequestBody JourneyProgressUpdateRequest request) {

        JourneyProgressResponse response = journeyService.updateProgress(progressId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{progressId}")
    @Operation(
        summary = "현재 진행률 조회",
        description = """
            현재 여행 진행률을 조회합니다.

            **조회 정보:**
            - 현재 누적 거리
            - 진행률 퍼센티지
            - 다음 랜드마크 정보
            - 수집한 스탬프 수
            - 총 랜드마크 수
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<JourneyProgressResponse> getProgress(
            @Parameter(description = "진행 ID")
            @PathVariable Long progressId) {

        JourneyProgressResponse response = journeyService.getProgress(progressId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "사용자 여행 목록",
        description = """
            사용자의 모든 여행 진행 상황을 조회합니다.

            **조회 정보:**
            - 진행 중인 여행 목록
            - 완료된 여행 목록
            - 일시정지된 여행 목록
            - 각 여행의 진행률 및 상태
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<List<JourneyProgressResponse>> getUserJourneys(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId) {

        List<JourneyProgressResponse> journeys = journeyService.getUserJourneys(userId);
        return ResponseEntity.ok(journeys);
    }
}