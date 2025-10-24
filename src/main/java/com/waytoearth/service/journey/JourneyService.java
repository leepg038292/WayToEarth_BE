package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.JourneyStartRequest;
import com.waytoearth.dto.request.journey.JourneyProgressUpdateRequest;
import com.waytoearth.dto.response.journey.JourneySummaryResponse;
import com.waytoearth.dto.response.journey.JourneyProgressResponse;
import com.waytoearth.dto.response.journey.JourneyCompletionEstimateResponse;
import com.waytoearth.dto.response.journey.JourneyRouteResponse;
import com.waytoearth.entity.journey.JourneyEntity;
import com.waytoearth.entity.enums.JourneyCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JourneyService {

    /**
     * 활성화된 여행 목록 조회
     */
    List<JourneySummaryResponse> getActiveJourneys();

    /**
     * 카테고리별 여행 목록 조회
     */
    List<JourneySummaryResponse> getJourneysByCategory(JourneyCategory category);

    /**
     * 여행 상세 조회
     */
    JourneyEntity getJourneyById(Long journeyId);

    /**
     * 여행 시작
     */
    JourneyProgressResponse startJourney(JourneyStartRequest request);

    /**
     * 여행 진행률 업데이트
     */
    JourneyProgressResponse updateProgress(Long progressId, JourneyProgressUpdateRequest request);

    /**
     * sessionId로 여행 진행률 업데이트
     */
    JourneyProgressResponse updateProgressBySessionId(String sessionId, JourneyProgressUpdateRequest request);

    /**
     * 현재 진행률 조회
     */
    JourneyProgressResponse getProgress(Long progressId);

    /**
     * 사용자의 여행 목록 조회
     */
    List<JourneyProgressResponse> getUserJourneys(Long userId);

    /**
     * 사용자의 특정 여정 진행률 조회
     */
    JourneyProgressResponse getUserJourneyProgress(Long userId, Long journeyId);

    /**
     * 제목으로 여행 검색
     */
    List<JourneySummaryResponse> searchJourneysByTitle(String keyword);

    /**
     * 여정 완주 예상 기간 계산
     */
    JourneyCompletionEstimateResponse calculateCompletionEstimate(Long journeyId, Integer runsPerWeek, Double averageDistancePerRun);

    /**
     * 여정 전체 경로 조회 (페이징 지원)
     */
    Page<JourneyRouteResponse> getJourneyRoutes(Long journeyId, Pageable pageable);

    /**
     * 여정 전체 경로 조회 (리스트)
     */
    List<JourneyRouteResponse> getJourneyRoutes(Long journeyId);

    /**
     * 여정 구간별 경로 조회
     */
    List<JourneyRouteResponse> getJourneyRoutesBySequenceRange(Long journeyId, Integer fromSequence, Integer toSequence);

    /**
     * 여정 구간별 경로 조회 (페이징 지원)
     */
    Page<JourneyRouteResponse> getJourneyRoutesBySequenceRange(Long journeyId, Integer fromSequence, Integer toSequence, Pageable pageable);

    /**
     * 여정 경로 통계 조회
     */
    JourneyRouteStatistics getJourneyRouteStatistics(Long journeyId);

    /**
     * 여정 경로 통계 정보
     */
    record JourneyRouteStatistics(
        Long totalRoutePoints,
        Integer maxSequence,
        Integer minSequence
    ) {}
}