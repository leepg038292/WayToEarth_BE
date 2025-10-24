package com.waytoearth.service.journey;

import com.waytoearth.dto.response.journey.LandmarkDetailResponse;
import com.waytoearth.dto.response.journey.LandmarkSummaryResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.entity.journey.LandmarkImage;
import com.waytoearth.entity.journey.LandmarkEntity;
import com.waytoearth.entity.journey.StoryCardEntity;
import com.waytoearth.entity.enums.StoryType;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.LandmarkImageRepository;
import com.waytoearth.repository.journey.StampRepository;
import com.waytoearth.repository.journey.StoryCardRepository;
import com.waytoearth.repository.journey.StoryCardImageRepository;
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
    private final LandmarkImageRepository landmarkImageRepository;
    private final StoryCardImageRepository storyCardImageRepository;
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

        // 갤러리 이미지 로드 및 주입 (정렬된 순서)
        var galleryImages = landmarkImageRepository.findByLandmarkIdOrderByOrderIndexAsc(landmarkId);
        landmark.setImages(galleryImages);

        // 스토리 카드 이미지 N+1 방지: 스토리 카드 ID로 이미지 일괄 로드 후 주입
        List<StoryCardResponse> storyCards;
        if (landmark.getStoryCards() != null && !landmark.getStoryCards().isEmpty()) {
            var storyCardIds = landmark.getStoryCards().stream()
                    .map(com.waytoearth.entity.journey.StoryCardEntity::getId)
                    .toList();
            var allImages = storyCardImageRepository
                    .findByStoryCardIdInOrderByStoryCardIdAscOrderIndexAsc(storyCardIds);

            java.util.Map<Long, java.util.List<com.waytoearth.entity.journey.StoryCardImage>> imagesByStoryId =
                    new java.util.HashMap<>();
            for (var img : allImages) {
                imagesByStoryId
                        .computeIfAbsent(img.getStoryCard().getId(), k -> new java.util.ArrayList<>())
                        .add(img);
            }
            // 주입 후 DTO 변환
            for (var sc : landmark.getStoryCards()) {
                var imgs = imagesByStoryId.get(sc.getId());
                if (imgs != null) sc.setImages(imgs);
            }
            storyCards = landmark.getStoryCards().stream()
                    .map(StoryCardResponse::from)
                    .toList();
        } else {
            storyCards = java.util.List.of();
        }

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
        if (storyCards.isEmpty()) return java.util.List.of();

        var ids = storyCards.stream().map(StoryCardEntity::getId).toList();
        var allImages = storyCardImageRepository.findByStoryCardIdInOrderByStoryCardIdAscOrderIndexAsc(ids);
        java.util.Map<Long, java.util.List<com.waytoearth.entity.journey.StoryCardImage>> byId = new java.util.HashMap<>();
        for (var img : allImages) {
            byId.computeIfAbsent(img.getStoryCard().getId(), k -> new java.util.ArrayList<>()).add(img);
        }
        for (var sc : storyCards) {
            var imgs = byId.get(sc.getId());
            if (imgs != null) sc.setImages(imgs);
        }
        return storyCards.stream().map(StoryCardResponse::from).toList();
    }

    @Override
    public List<StoryCardResponse> getStoryCardsByType(Long landmarkId, StoryType type) {
        List<StoryCardEntity> storyCards = storyCardRepository.findByLandmarkIdAndTypeOrderByOrderIndex(landmarkId, type);
        if (storyCards.isEmpty()) return java.util.List.of();

        var ids = storyCards.stream().map(StoryCardEntity::getId).toList();
        var allImages = storyCardImageRepository.findByStoryCardIdInOrderByStoryCardIdAscOrderIndexAsc(ids);
        java.util.Map<Long, java.util.List<com.waytoearth.entity.journey.StoryCardImage>> byId = new java.util.HashMap<>();
        for (var img : allImages) {
            byId.computeIfAbsent(img.getStoryCard().getId(), k -> new java.util.ArrayList<>()).add(img);
        }
        for (var sc : storyCards) {
            var imgs = byId.get(sc.getId());
            if (imgs != null) sc.setImages(imgs);
        }
        return storyCards.stream().map(StoryCardResponse::from).toList();
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

    @Override
    @Transactional
    public void addLandmarkImage(Long landmarkId, String imageUrl) {
        LandmarkEntity landmark = landmarkRepository.findById(landmarkId)
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + landmarkId));

        var existing = landmarkImageRepository.findByLandmarkIdOrderByOrderIndexAsc(landmarkId);
        int nextOrder = existing.size();

        LandmarkImage img = LandmarkImage.builder()
                .landmark(landmark)
                .imageUrl(imageUrl)
                .orderIndex(nextOrder)
                .build();
        landmarkImageRepository.save(img);
    }

    @Override
    @Transactional
    public void deleteLandmarkImage(Long imageId) {
        landmarkImageRepository.deleteById(imageId);
    }

    @Override
    @Transactional
    public void reorderLandmarkImages(Long landmarkId, java.util.List<Long> imageIds) {
        var images = landmarkImageRepository.findByLandmarkIdOrderByOrderIndexAsc(landmarkId);
        java.util.Map<Long, Integer> orderMap = new java.util.HashMap<>();
        for (int i = 0; i < imageIds.size(); i++) {
            orderMap.put(imageIds.get(i), i);
        }
        for (var img : images) {
            Integer newOrder = orderMap.get(img.getId());
            if (newOrder != null) {
                img.setOrderIndex(newOrder);
            }
        }
        // JPA dirty checking으로 저장
    }
}
