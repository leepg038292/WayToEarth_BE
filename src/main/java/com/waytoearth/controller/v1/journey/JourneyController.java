package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.journey.JourneyStartRequest;
import com.waytoearth.dto.response.journey.JourneySummaryResponse;
import com.waytoearth.dto.response.journey.JourneyProgressResponse;
import com.waytoearth.dto.response.journey.JourneyCompletionEstimateResponse;
import com.waytoearth.entity.Journey.JourneyEntity;
import com.waytoearth.entity.enums.JourneyCategory;
import com.waytoearth.service.Journey.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/journeys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journey API", description = "여행 관리 API")
public class JourneyController {

    private final JourneyService journeyService;

    @GetMapping
    @Operation(
        summary = "여행 목록 조회",
        description = """
            활성화된 모든 여행 목록을 조회합니다.

            **응답 정보:**
            - 여정 기본 정보 (제목, 설명, 총 거리)
            - 완주자 통계 ("이 여정을 완주한 러너 X명")
            - 랜드마크 개수
            - 예상 완주 기간
            - 난이도 (EASY/MEDIUM/HARD)
            """,
        tags = {"Journey API"}
    )
    public ResponseEntity<List<JourneySummaryResponse>> getJourneys(
            @Parameter(description = "카테고리 필터")
            @RequestParam(required = false) JourneyCategory category) {

        List<JourneySummaryResponse> journeys = (category != null)
                ? journeyService.getJourneysByCategory(category)
                : journeyService.getActiveJourneys();

        return ResponseEntity.ok(journeys);
    }

    @GetMapping("/{journeyId}")
    @Operation(summary = "여행 상세 조회", description = "특정 여행의 상세 정보를 조회합니다.")
    public ResponseEntity<JourneyEntity> getJourney(
            @Parameter(description = "여행 ID")
            @PathVariable Long journeyId) {

        JourneyEntity journey = journeyService.getJourneyById(journeyId);
        return ResponseEntity.ok(journey);
    }

    @PostMapping("/{journeyId}/start")
    @Operation(summary = "여행 시작", description = "새로운 여행을 시작합니다.")
    public ResponseEntity<JourneyProgressResponse> startJourney(
            @Parameter(description = "여행 ID")
            @PathVariable Long journeyId,
            @Parameter(description = "사용자 ID")
            @RequestParam Long userId) {

        JourneyStartRequest request = new JourneyStartRequest(userId, journeyId);
        JourneyProgressResponse response = journeyService.startJourney(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "여행 검색", description = "제목으로 여행을 검색합니다.")
    public ResponseEntity<List<JourneySummaryResponse>> searchJourneys(
            @Parameter(description = "검색 키워드")
            @RequestParam String keyword) {

        List<JourneySummaryResponse> journeys = journeyService.searchJourneysByTitle(keyword);
        return ResponseEntity.ok(journeys);
    }

    @GetMapping("/{journeyId}/completion-estimate")
    @Operation(
        summary = "완주 예상 기간 계산",
        description = "사용자의 러닝 패턴(주당 횟수, 평균 거리)에 따른 여정 완주 예상 기간을 계산합니다.",
        tags = {"Journey API"}
    )
    public ResponseEntity<JourneyCompletionEstimateResponse> getCompletionEstimate(
            @Parameter(description = "여행 ID")
            @PathVariable Long journeyId,
            @Parameter(description = "주당 러닝 횟수", example = "3")
            @RequestParam(defaultValue = "3") Integer runsPerWeek,
            @Parameter(description = "1회 평균 거리 (km)", example = "5.0")
            @RequestParam(defaultValue = "5.0") Double averageDistancePerRun) {

        JourneyCompletionEstimateResponse estimate = journeyService.calculateCompletionEstimate(
                journeyId, runsPerWeek, averageDistancePerRun);
        return ResponseEntity.ok(estimate);
    }
}