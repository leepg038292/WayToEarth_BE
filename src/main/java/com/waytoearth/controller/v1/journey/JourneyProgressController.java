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
        summary = "진행률 업데이트 (progressId 사용)",
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

            **참고:** progressId를 알고 있는 경우 사용하세요.
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

    @PutMapping("/session/{sessionId}")
    @Operation(
        summary = "진행률 업데이트 (sessionId 사용, 권장)",
        description = """
            러닝 세션 ID로 여행 진행률을 업데이트합니다.

            **사용 시나리오:**
            1. 여정 시작 시 sessionId 받음 (예: "journey-23-1733234567890")
            2. 러닝 완료 후 같은 sessionId로 진행률 업데이트
            3. 어떤 여정인지 자동으로 매칭

            **업데이트 정보:**
            - 이번 세션에서 뛴 거리
            - 현재 위치 정보
            - 운동 시간 및 칼로리
            - 평균 페이스

            **자동 처리:**
            - sessionId로 여정 자동 식별
            - 총 누적 거리 계산
            - 진행률 퍼센티지 계산
            - 다음 랜드마크 정보 업데이트
            - 러닝 레코드 완료 처리
            - 100% 달성 시 자동 완료 처리

            **장점:**
            - progressId를 몰라도 됨
            - 가장 최근 러닝한 여정 자동 식별
            - 더 간편한 API 사용
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<JourneyProgressResponse> updateProgressBySessionId(
            @Parameter(description = "러닝 세션 ID", example = "journey-23-1733234567890")
            @PathVariable String sessionId,
            @Valid @RequestBody JourneyProgressUpdateRequest request) {

        JourneyProgressResponse response = journeyService.updateProgressBySessionId(sessionId, request);
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
            - 각 여행의 ID, 제목, 총 거리
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<List<JourneyProgressResponse>> getUserJourneys(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId) {

        List<JourneyProgressResponse> journeys = journeyService.getUserJourneys(userId);
        return ResponseEntity.ok(journeys);
    }

    @GetMapping("/user/{userId}/journey/{journeyId}")
    @Operation(
        summary = "사용자의 특정 여정 진행률 조회",
        description = """
            사용자의 특정 여정 진행 상황을 조회합니다.

            **조회 정보:**
            - 여정 ID 및 제목
            - 현재 누적 거리
            - 진행률 퍼센티지
            - 다음 랜드마크 정보
            - 수집한 스탬프 수
            - 총 랜드마크 수

            **사용 예시:**
            - 여정 리스트에서 특정 여정 선택 시
            - 해당 여정의 정확한 진행률 조회
            """,
        tags = {"Journey Progress API"}
    )
    public ResponseEntity<JourneyProgressResponse> getUserJourneyProgress(
            @Parameter(description = "사용자 ID")
            @PathVariable Long userId,
            @Parameter(description = "여정 ID")
            @PathVariable Long journeyId) {

        JourneyProgressResponse progress = journeyService.getUserJourneyProgress(userId, journeyId);
        return ResponseEntity.ok(progress);
    }
}