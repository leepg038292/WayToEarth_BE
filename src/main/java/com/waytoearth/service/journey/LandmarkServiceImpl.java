package com.waytoearth.service.journey;

import com.waytoearth.dto.response.journey.LandmarkDetailResponse;
import com.waytoearth.dto.response.journey.LandmarkSummaryResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.entity.journey.LandmarkEntity;
import com.waytoearth.entity.journey.StoryCardEntity;
import com.waytoearth.entity.enums.StoryType;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.StampRepository;
import com.waytoearth.repository.journey.StoryCardRepository;
import com.waytoearth.repository.journey.UserJourneyProgressRepository;
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
    public List<StoryCardResponse> getStoryCardsByType(Long landmarkId, StoryType type) {
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

    @Override
    @Transactional
    public void updateLandmarkImage(Long landmarkId, String imageUrl) {
        log.info("랜드마크 이미지 업데이트: landmarkId={}, imageUrl={}", landmarkId, imageUrl);

        LandmarkEntity landmark = landmarkRepository.findById(landmarkId)
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + landmarkId));

        landmark.setImageUrl(imageUrl);
        landmarkRepository.save(landmark);

        log.info("랜드마크 이미지 업데이트 완료: landmarkId={}", landmarkId);
    }
}