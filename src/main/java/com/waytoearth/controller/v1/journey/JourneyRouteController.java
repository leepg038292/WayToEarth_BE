package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.response.journey.JourneyRouteResponse;
import com.waytoearth.service.journey.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/v1/journeys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journey Route API", description = "여정 경로 관리 API")
public class JourneyRouteController {

    private final JourneyService journeyService;

    @GetMapping("/{journeyId}/routes")
    @Operation(
        summary = "여정 경로 조회",
        description = """
            여정의 경로 좌표들을 조회합니다.

            **조회 옵션:**
            - 전체 경로 조회 (페이징 지원)
            - 구간별 경로 조회 (from, to 파라미터 사용)

            **페이징:**
            - page: 페이지 번호 (0부터 시작)
            - size: 페이지 크기 (기본 100개)

            **구간 조회:**
            - from: 시작 sequence 번호
            - to: 끝 sequence 번호

            **응답 데이터:**
            - 위도, 경도 좌표
            - sequence 순서로 정렬
            - 고도 정보 (있는 경우)
            - 구간 설명 (있는 경우)
            """,
        tags = {"Journey Route API"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "경로 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "content": [
                        {
                          "id": 1,
                          "latitude": 37.5665,
                          "longitude": 126.9780,
                          "sequence": 1,
                          "altitude": 120.5,
                          "description": "한강대교 진입"
                        },
                        {
                          "id": 2,
                          "latitude": 37.5670,
                          "longitude": 126.9785,
                          "sequence": 2,
                          "altitude": 118.2,
                          "description": "한강대교 중앙"
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 100
                      },
                      "totalElements": 1500,
                      "totalPages": 15
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정을 찾을 수 없음")
    })
    public ResponseEntity<Page<JourneyRouteResponse>> getJourneyRoutes(
            @Parameter(description = "여정 ID", example = "1")
            @PathVariable Long journeyId,

            @Parameter(description = "시작 sequence 번호 (구간 조회용)", example = "1")
            @RequestParam(required = false) Integer from,

            @Parameter(description = "끝 sequence 번호 (구간 조회용)", example = "100")
            @RequestParam(required = false) Integer to,

            @PageableDefault(size = 100, sort = "sequence", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<JourneyRouteResponse> routes;

        if (from != null && to != null) {
            // 구간별 조회 (페이징)
            routes = journeyService.getJourneyRoutesBySequenceRange(journeyId, from, to, pageable);
            log.info("구간별 여정 경로 조회: journeyId={}, from={}, to={}, page={}",
                    journeyId, from, to, pageable.getPageNumber());
        } else {
            // 전체 조회 (페이징)
            routes = journeyService.getJourneyRoutes(journeyId, pageable);
            log.info("전체 여정 경로 조회: journeyId={}, page={}",
                    journeyId, pageable.getPageNumber());
        }

        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{journeyId}/routes/all")
    @Operation(
        summary = "여정 전체 경로 조회 (리스트)",
        description = """
            여정의 모든 경로 좌표를 리스트로 조회합니다.
            페이징 없이 전체 데이터를 한번에 반환합니다.

            **주의사항:**
            - 경로가 긴 여정의 경우 응답 크기가 클 수 있습니다
            - 메모리 효율을 위해 가급적 페이징 API 사용을 권장합니다

            **사용 케이스:**
            - 전체 경로를 한번에 지도에 그려야 하는 경우
            - 경로 데이터를 캐싱하는 경우
            """,
        tags = {"Journey Route API"}
    )
    public ResponseEntity<List<JourneyRouteResponse>> getAllJourneyRoutes(
            @Parameter(description = "여정 ID", example = "1")
            @PathVariable Long journeyId,

            @Parameter(description = "시작 sequence 번호 (구간 조회용)", example = "1")
            @RequestParam(required = false) Integer from,

            @Parameter(description = "끝 sequence 번호 (구간 조회용)", example = "100")
            @RequestParam(required = false) Integer to
    ) {
        List<JourneyRouteResponse> routes;

        if (from != null && to != null) {
            // 구간별 조회
            routes = journeyService.getJourneyRoutesBySequenceRange(journeyId, from, to);
            log.info("구간별 여정 전체 경로 조회: journeyId={}, from={}, to={}", journeyId, from, to);
        } else {
            // 전체 조회
            routes = journeyService.getJourneyRoutes(journeyId);
            log.info("여정 전체 경로 조회: journeyId={}", journeyId);
        }

        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{journeyId}/routes/statistics")
    @Operation(
        summary = "여정 경로 통계",
        description = """
            여정의 경로 관련 통계 정보를 조회합니다.

            **제공 정보:**
            - 총 경로 포인트 수
            - 최대 sequence 번호
            - 최소 sequence 번호

            **활용 방안:**
            - 구간별 조회를 위한 범위 설정
            - 경로 데이터 존재 여부 확인
            - 페이징 계산을 위한 기초 데이터
            """,
        tags = {"Journey Route API"}
    )
    public ResponseEntity<JourneyService.JourneyRouteStatistics> getJourneyRouteStatistics(
            @Parameter(description = "여정 ID", example = "1")
            @PathVariable Long journeyId
    ) {
        JourneyService.JourneyRouteStatistics statistics = journeyService.getJourneyRouteStatistics(journeyId);
        log.info("여정 경로 통계 조회: journeyId={}, totalPoints={}",
                journeyId, statistics.totalRoutePoints());

        return ResponseEntity.ok(statistics);
    }
}