package com.waytoearth.service.Journey;

import com.waytoearth.dto.Journey.response.LandmarkDetailResponse;
import com.waytoearth.dto.Journey.response.LandmarkSummaryResponse;
import com.waytoearth.dto.Journey.response.StoryCardResponse;
import com.waytoearth.entity.Journey.StoryCardEntity;

import java.util.List;

public interface LandmarkService {

    /**
     * 여행별 랜드마크 목록 조회
     */
    List<LandmarkSummaryResponse> getLandmarksByJourneyId(Long journeyId);

    /**
     * 랜드마크 상세 정보 조회
     */
    LandmarkDetailResponse getLandmarkDetail(Long landmarkId, Long userId);

    /**
     * 랜드마크의 스토리 카드 목록 조회
     */
    List<StoryCardResponse> getStoryCardsByLandmarkId(Long landmarkId);

    /**
     * 타입별 스토리 카드 조회
     */
    List<StoryCardResponse> getStoryCardsByType(Long landmarkId, StoryCardEntity.StoryType type);

    /**
     * 스토리 카드 상세 조회
     */
    StoryCardResponse getStoryCardById(Long storyCardId);

    /**
     * 다음 랜드마크 조회
     */
    LandmarkSummaryResponse getNextLandmark(Long journeyId, Double currentDistance);
}