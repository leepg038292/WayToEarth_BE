package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.StoryCardCreateRequest;
import com.waytoearth.dto.request.journey.StoryCardUpdateRequest;
import com.waytoearth.dto.response.journey.StoryCardResponse;

/**
 * 스토리 카드 서비스 인터페이스
 * 스토리 카드 CRUD 작업을 처리합니다.
 */
public interface StoryCardService {

    /**
     * 스토리 카드 생성
     *
     * @param request 스토리 카드 생성 요청
     * @return 생성된 스토리 카드 응답
     */
    StoryCardResponse createStoryCard(StoryCardCreateRequest request);

    /**
     * 스토리 카드 수정
     *
     * @param storyId 스토리 카드 ID
     * @param request 스토리 카드 수정 요청
     * @return 수정된 스토리 카드 응답
     */
    StoryCardResponse updateStoryCard(Long storyId, StoryCardUpdateRequest request);

    /**
     * 스토리 카드 삭제
     *
     * @param storyId 스토리 카드 ID
     */
    void deleteStoryCard(Long storyId);

    /**
     * 스토리 카드 조회
     *
     * @param storyId 스토리 카드 ID
     * @return 스토리 카드 응답
     */
    StoryCardResponse getStoryCard(Long storyId);

    // 갤러리 이미지 관리
    void addStoryCardImage(Long storyId, String imageUrl);

    void deleteStoryCardImage(Long imageId);

    void reorderStoryCardImages(Long storyId, java.util.List<Long> imageIds);
}
