package com.waytoearth.repository.journey;

import com.waytoearth.dto.response.journey.JourneyProgressResponse;
import java.util.List;

public interface UserJourneyProgressRepositoryCustom {

    /**
     * 사용자 ID로 모든 여정 진행 상태를 조회 (N+1 문제 해결)
     * - 한 번의 쿼리로 각 여정 진행에 필요한 모든 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return JourneyProgressResponse 리스트
     */
    List<JourneyProgressResponse> findProgressResponsesByUserId(Long userId);
}
