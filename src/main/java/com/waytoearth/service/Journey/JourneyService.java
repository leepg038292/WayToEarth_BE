package com.waytoearth.service.Journey;

import com.waytoearth.dto.Journey.request.JourneyStartRequest;
import com.waytoearth.dto.Journey.request.JourneyProgressUpdateRequest;
import com.waytoearth.dto.Journey.response.JourneySummaryResponse;
import com.waytoearth.dto.Journey.response.JourneyProgressResponse;
import com.waytoearth.dto.Journey.response.JourneyCompletionEstimateResponse;
import com.waytoearth.entity.Journey.JourneyEntity;

import java.util.List;

public interface JourneyService {

    /**
     * 활성화된 여행 목록 조회
     */
    List<JourneySummaryResponse> getActiveJourneys();

    /**
     * 카테고리별 여행 목록 조회
     */
    List<JourneySummaryResponse> getJourneysByCategory(JourneyEntity.Category category);

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
     * 현재 진행률 조회
     */
    JourneyProgressResponse getProgress(Long progressId);

    /**
     * 사용자의 여행 목록 조회
     */
    List<JourneyProgressResponse> getUserJourneys(Long userId);

    /**
     * 제목으로 여행 검색
     */
    List<JourneySummaryResponse> searchJourneysByTitle(String keyword);

    /**
     * 여정 완주 예상 기간 계산
     */
    JourneyCompletionEstimateResponse calculateCompletionEstimate(Long journeyId, Integer runsPerWeek, Double averageDistancePerRun);
}