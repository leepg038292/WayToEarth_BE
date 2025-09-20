package com.waytoearth.repository.Journey;

import com.waytoearth.entity.Journey.JourneyEntity;
import com.waytoearth.entity.enums.JourneyCategory;
import com.waytoearth.entity.enums.JourneyDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JourneyRepository extends JpaRepository<JourneyEntity, Long> {

    /**
     * 활성화된 여행 목록 조회
     */
    List<JourneyEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 카테고리별 활성화된 여행 목록 조회
     */
    List<JourneyEntity> findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(JourneyCategory category);

    /**
     * 난이도별 활성화된 여행 목록 조회
     */
    List<JourneyEntity> findByIsActiveTrueAndDifficultyOrderByCreatedAtDesc(JourneyDifficulty difficulty);

    /**
     * 활성화된 여행 상세 조회 (랜드마크 포함)
     */
    @Query("SELECT j FROM JourneyEntity j LEFT JOIN FETCH j.landmarks l WHERE j.id = :journeyId AND j.isActive = true ORDER BY l.orderIndex")
    Optional<JourneyEntity> findActiveJourneyWithLandmarks(@Param("journeyId") Long journeyId);

    /**
     * 제목으로 검색
     */
    @Query("SELECT j FROM JourneyEntity j WHERE j.isActive = true AND j.title LIKE %:keyword% ORDER BY j.createdAt DESC")
    List<JourneyEntity> searchByTitle(@Param("keyword") String keyword);

    /**
     * 거리 범위로 검색
     */
    List<JourneyEntity> findByIsActiveTrueAndTotalDistanceKmBetweenOrderByTotalDistanceKmAsc(
            Double minDistance, Double maxDistance);
}