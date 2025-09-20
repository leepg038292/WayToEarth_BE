package com.waytoearth.repository.Journey;

import com.waytoearth.entity.Journey.LandmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandmarkRepository extends JpaRepository<LandmarkEntity, Long> {

    /**
     * 여행별 랜드마크 목록 조회 (순서대로)
     */
    List<LandmarkEntity> findByJourneyIdOrderByOrderIndex(Long journeyId);

    /**
     * 랜드마크 상세 조회 (스토리 카드 포함)
     */
    @Query("SELECT l FROM LandmarkEntity l LEFT JOIN FETCH l.storyCards s WHERE l.id = :landmarkId ORDER BY s.orderIndex")
    Optional<LandmarkEntity> findLandmarkWithStoryCards(@Param("landmarkId") Long landmarkId);

    /**
     * 거리 기준으로 다음 랜드마크 찾기
     */
    @Query("SELECT l FROM LandmarkEntity l WHERE l.journey.id = :journeyId AND l.distanceFromStart > :currentDistance ORDER BY l.distanceFromStart ASC")
    Optional<LandmarkEntity> findNextLandmarkByDistance(@Param("journeyId") Long journeyId, @Param("currentDistance") Double currentDistance);

    /**
     * 국가별 랜드마크 조회
     */
    List<LandmarkEntity> findByCountryCodeOrderByOrderIndex(String countryCode);

    /**
     * 도시별 랜드마크 조회
     */
    List<LandmarkEntity> findByCityNameOrderByOrderIndex(String cityName);

    /**
     * 여행의 총 랜드마크 수 조회
     */
    @Query("SELECT COUNT(l) FROM LandmarkEntity l WHERE l.journey.id = :journeyId")
    Long countLandmarksByJourneyId(@Param("journeyId") Long journeyId);
}