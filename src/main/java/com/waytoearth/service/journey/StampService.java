package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.StampCollectRequest;
import com.waytoearth.dto.response.journey.StampResponse;
import com.waytoearth.security.AuthenticatedUser;

import java.util.List;

public interface StampService {

    /**
     * 스탬프 수집
     */
    StampResponse collectStamp(AuthenticatedUser user, StampCollectRequest request);

    /**
     * 사용자별 스탬프 목록 조회
     */
    List<StampResponse> getStampsByUserId(Long userId);

    /**
     * 여행 진행별 스탬프 목록 조회
     */
    List<StampResponse> getStampsByProgressId(AuthenticatedUser user, Long progressId);


    /**
     * 스탬프 수집 가능 여부 확인
     */
    boolean canCollectStamp(AuthenticatedUser user, Long progressId, Long landmarkId, Double userLatitude, Double userLongitude);

    /**
     * 사용자의 총 스탬프 통계
     */
    StampStatistics getStampStatistics(Long userId);

    /**
     * 스탬프 통계 레코드
     */
    record StampStatistics(
        Long totalStamps,
        Long completedJourneys
    ) {}
}
