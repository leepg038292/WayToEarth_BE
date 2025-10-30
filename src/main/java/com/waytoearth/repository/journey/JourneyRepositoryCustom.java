package com.waytoearth.repository.journey;

import com.waytoearth.dto.response.journey.JourneySummaryResponse;
import com.waytoearth.entity.enums.JourneyCategory;

import java.util.List;

/**
 * Journey Repository의 QueryDSL 커스텀 메서드 정의
 */
public interface JourneyRepositoryCustom {

    /**
     * 활성화된 여행 목록 조회 (N+1 해결)
     * - 한 번의 쿼리로 journey + landmarkCount + completedRunners 조회
     */
    List<JourneySummaryResponse> findActiveJourneysWithStats();

    /**
     * 카테고리별 활성화된 여행 목록 조회 (N+1 해결)
     * - 한 번의 쿼리로 journey + landmarkCount + completedRunners 조회
     */
    List<JourneySummaryResponse> findJourneysByCategoryWithStats(JourneyCategory category);

    /**
     * 제목으로 검색 (N+1 해결)
     * - 한 번의 쿼리로 journey + landmarkCount + completedRunners 조회
     */
    List<JourneySummaryResponse> searchJourneysByTitleWithStats(String keyword);
}
