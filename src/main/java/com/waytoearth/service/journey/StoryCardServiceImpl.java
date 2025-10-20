package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.StoryCardCreateRequest;
import com.waytoearth.dto.request.journey.StoryCardUpdateRequest;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.entity.journey.LandmarkEntity;
import com.waytoearth.entity.journey.StoryCardEntity;
import com.waytoearth.exception.LandmarkNotFoundException;
import com.waytoearth.exception.StoryCardNotFoundException;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.StoryCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스토리 카드 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class StoryCardServiceImpl implements StoryCardService {

    private final StoryCardRepository storyCardRepository;
    private final LandmarkRepository landmarkRepository;

    @Override
    @Transactional
    public StoryCardResponse createStoryCard(StoryCardCreateRequest request) {
        log.info("스토리 카드 생성 요청: landmarkId={}, title={}, type={}",
                request.landmarkId(), request.title(), request.type());

        // 랜드마크 존재 확인
        LandmarkEntity landmark = landmarkRepository.findById(request.landmarkId())
                .orElseThrow(() -> new LandmarkNotFoundException(request.landmarkId()));

        // 스토리 카드 생성
        StoryCardEntity storyCard = StoryCardEntity.builder()
                .landmark(landmark)
                .title(request.title())
                .content(request.content())
                .imageUrl(request.imageUrl())
                .type(request.type())
                .orderIndex(request.orderIndex())
                .build();

        StoryCardEntity savedStoryCard = storyCardRepository.save(storyCard);
        log.info("스토리 카드 생성 완료: storyCardId={}", savedStoryCard.getId());

        return StoryCardResponse.from(savedStoryCard);
    }

    @Override
    @Transactional
    public StoryCardResponse updateStoryCard(Long storyId, StoryCardUpdateRequest request) {
        log.info("스토리 카드 수정 요청: storyId={}, title={}", storyId, request.title());

        // 스토리 카드 존재 확인
        StoryCardEntity storyCard = storyCardRepository.findById(storyId)
                .orElseThrow(() -> new StoryCardNotFoundException(storyId));

        // 스토리 카드 수정
        storyCard.setTitle(request.title());
        storyCard.setContent(request.content());
        storyCard.setImageUrl(request.imageUrl());
        storyCard.setType(request.type());
        storyCard.setOrderIndex(request.orderIndex());

        StoryCardEntity updatedStoryCard = storyCardRepository.save(storyCard);
        log.info("스토리 카드 수정 완료: storyCardId={}", updatedStoryCard.getId());

        return StoryCardResponse.from(updatedStoryCard);
    }

    @Override
    @Transactional
    public void deleteStoryCard(Long storyId) {
        log.info("스토리 카드 삭제 요청: storyId={}", storyId);

        // 스토리 카드 존재 확인
        StoryCardEntity storyCard = storyCardRepository.findById(storyId)
                .orElseThrow(() -> new StoryCardNotFoundException(storyId));

        storyCardRepository.delete(storyCard);
        log.info("스토리 카드 삭제 완료: storyCardId={}", storyId);
    }

    @Override
    public StoryCardResponse getStoryCard(Long storyId) {
        log.info("스토리 카드 조회 요청: storyId={}", storyId);

        StoryCardEntity storyCard = storyCardRepository.findById(storyId)
                .orElseThrow(() -> new StoryCardNotFoundException(storyId));

        return StoryCardResponse.from(storyCard);
    }
}
