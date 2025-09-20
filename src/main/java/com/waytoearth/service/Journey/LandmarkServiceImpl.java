package com.waytoearth.service.Journey;

import com.waytoearth.dto.Journey.response.LandmarkDetailResponse;
import com.waytoearth.dto.Journey.response.LandmarkSummaryResponse;
import com.waytoearth.dto.Journey.response.StoryCardResponse;
import com.waytoearth.entity.Journey.LandmarkEntity;
import com.waytoearth.entity.Journey.StoryCardEntity;
import com.waytoearth.repository.Journey.LandmarkRepository;
import com.waytoearth.repository.Journey.StampRepository;
import com.waytoearth.repository.Journey.StoryCardRepository;
import com.waytoearth.repository.Journey.UserJourneyProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LandmarkServiceImpl implements LandmarkService {

    private final LandmarkRepository landmarkRepository;
    private final StoryCardRepository storyCardRepository;
    private final StampRepository stampRepository;
    private final UserJourneyProgressRepository progressRepository;

    @Override
    public List<LandmarkSummaryResponse> getLandmarksByJourneyId(Long journeyId) {
        List<LandmarkEntity> landmarks = landmarkRepository.findByJourneyIdOrderByOrderIndex(journeyId);

        return landmarks.stream()
                .map(LandmarkSummaryResponse::from)
                .toList();
    }

    @Override
    public LandmarkDetailResponse getLandmarkDetail(Long landmarkId, Long userId) {
        LandmarkEntity landmark = landmarkRepository.findLandmarkWithStoryCards(landmarkId)
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + landmarkId));

        // 스토리 카드 변환
        List<StoryCardResponse> storyCards = landmark.getStoryCards().stream()
                .map(StoryCardResponse::from)
                .toList();

        // 사용자의 스탬프 수집 여부 확인
        Boolean hasStamp = false;
        if (userId != null) {
            hasStamp = progressRepository.findByUserIdAndJourneyId(userId, landmark.getJourney().getId())
                    .map(progress -> stampRepository.findByUserJourneyProgressIdAndLandmarkId(
                            progress.getId(), landmarkId).isPresent())
                    .orElse(false);
        }

        return LandmarkDetailResponse.from(landmark, storyCards, hasStamp);
    }

    @Override
    public List<StoryCardResponse> getStoryCardsByLandmarkId(Long landmarkId) {
        List<StoryCardEntity> storyCards = storyCardRepository.findByLandmarkIdOrderByOrderIndex(landmarkId);

        return storyCards.stream()
                .map(StoryCardResponse::from)
                .toList();
    }

    @Override
    public List<StoryCardResponse> getStoryCardsByType(Long landmarkId, StoryCardEntity.StoryType type) {
        List<StoryCardEntity> storyCards = storyCardRepository.findByLandmarkIdAndTypeOrderByOrderIndex(landmarkId, type);

        return storyCards.stream()
                .map(StoryCardResponse::from)
                .toList();
    }

    @Override
    public StoryCardResponse getStoryCardById(Long storyCardId) {
        StoryCardEntity storyCard = storyCardRepository.findById(storyCardId)
                .orElseThrow(() -> new IllegalArgumentException("스토리 카드를 찾을 수 없습니다: " + storyCardId));

        return StoryCardResponse.from(storyCard);
    }

    @Override
    public LandmarkSummaryResponse getNextLandmark(Long journeyId, Double currentDistance) {
        return landmarkRepository.findNextLandmarkByDistance(journeyId, currentDistance)
                .map(LandmarkSummaryResponse::from)
                .orElse(null);
    }
}