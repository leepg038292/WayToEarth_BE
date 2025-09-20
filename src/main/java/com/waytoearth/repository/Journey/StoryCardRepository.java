package com.waytoearth.repository.Journey;

import com.waytoearth.entity.Journey.StoryCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryCardRepository extends JpaRepository<StoryCardEntity, Long> {

    /**
     * 랜드마크별 스토리 카드 목록 조회 (순서대로)
     */
    List<StoryCardEntity> findByLandmarkIdOrderByOrderIndex(Long landmarkId);

    /**
     * 타입별 스토리 카드 조회
     */
    List<StoryCardEntity> findByLandmarkIdAndTypeOrderByOrderIndex(Long landmarkId, StoryCardEntity.StoryType type);

}