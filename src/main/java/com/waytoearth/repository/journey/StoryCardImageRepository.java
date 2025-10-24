package com.waytoearth.repository.journey;

import com.waytoearth.entity.journey.StoryCardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryCardImageRepository extends JpaRepository<StoryCardImage, Long> {
    List<StoryCardImage> findByStoryCardIdOrderByOrderIndexAsc(Long storyCardId);

    // 다건 스토리 카드의 이미지를 정렬된 순서로 한번에 로드 (N+1 방지)
    List<StoryCardImage> findByStoryCardIdInOrderByStoryCardIdAscOrderIndexAsc(java.util.List<Long> storyCardIds);
}
