package com.waytoearth.repository.journey;

import com.waytoearth.entity.Journey.StampEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StampRepository extends JpaRepository<StampEntity, Long> {

    /**
     * 사용자 여행 진행별 수집된 스탬프 목록
     */
    @Query("SELECT s FROM StampEntity s JOIN FETCH s.landmark WHERE s.userJourneyProgress.id = :progressId ORDER BY s.collectedAt DESC")
    List<StampEntity> findByUserJourneyProgressIdWithLandmark(@Param("progressId") Long progressId);

    /**
     * 사용자별 모든 수집된 스탬프 목록
     */
    @Query("SELECT s FROM StampEntity s JOIN FETCH s.landmark JOIN FETCH s.userJourneyProgress ujp WHERE ujp.user.id = :userId ORDER BY s.collectedAt DESC")
    List<StampEntity> findByUserIdWithLandmark(@Param("userId") Long userId);

    /**
     * 특정 랜드마크의 스탬프 수집 여부 확인
     */
    Optional<StampEntity> findByUserJourneyProgressIdAndLandmarkId(Long progressId, Long landmarkId);

    /**
     * 사용자의 총 스탬프 수
     */
    @Query("SELECT COUNT(s) FROM StampEntity s WHERE s.userJourneyProgress.user.id = :userId")
    Long countStampsByUserId(@Param("userId") Long userId);

    /**
     * 랜드마크별 스탬프 수집자 수
     */
    @Query("SELECT COUNT(s) FROM StampEntity s WHERE s.landmark.id = :landmarkId")
    Long countCollectorsByLandmarkId(@Param("landmarkId") Long landmarkId);

    /**
     * 여행별 수집된 스탬프 수
     */
    @Query("SELECT COUNT(s) FROM StampEntity s WHERE s.userJourneyProgress.id = :progressId")
    Long countStampsByProgressId(@Param("progressId") Long progressId);
}