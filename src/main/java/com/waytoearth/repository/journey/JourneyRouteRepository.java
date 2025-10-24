package com.waytoearth.repository.journey;

import com.waytoearth.entity.journey.JourneyRouteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourneyRouteRepository extends JpaRepository<JourneyRouteEntity, Long> {

    /**
     * 여정 ID로 경로 조회 (순서대로 정렬)
     */
    List<JourneyRouteEntity> findByJourneyIdOrderBySequenceAsc(Long journeyId);

    /**
     * 여정 ID로 경로 조회 (페이징 지원)
     */
    @Query("SELECT jr FROM JourneyRouteEntity jr WHERE jr.journey.id = :journeyId ORDER BY jr.sequence ASC")
    Page<JourneyRouteEntity> findByJourneyIdWithPaging(@Param("journeyId") Long journeyId, Pageable pageable);

    /**
     * 여정 ID와 순서 범위로 경로 조회 (구간별 조회)
     */
    @Query("SELECT jr FROM JourneyRouteEntity jr " +
           "WHERE jr.journey.id = :journeyId " +
           "AND jr.sequence BETWEEN :fromSequence AND :toSequence " +
           "ORDER BY jr.sequence ASC")
    List<JourneyRouteEntity> findByJourneyIdAndSequenceRange(
            @Param("journeyId") Long journeyId,
            @Param("fromSequence") Integer fromSequence,
            @Param("toSequence") Integer toSequence
    );

    /**
     * 여정 ID와 순서 범위로 경로 조회 (페이징 지원)
     */
    @Query("SELECT jr FROM JourneyRouteEntity jr " +
           "WHERE jr.journey.id = :journeyId " +
           "AND jr.sequence BETWEEN :fromSequence AND :toSequence " +
           "ORDER BY jr.sequence ASC")
    Page<JourneyRouteEntity> findByJourneyIdAndSequenceRangeWithPaging(
            @Param("journeyId") Long journeyId,
            @Param("fromSequence") Integer fromSequence,
            @Param("toSequence") Integer toSequence,
            Pageable pageable
    );

    /**
     * 여정의 경로 개수 조회
     */
    Long countByJourneyId(Long journeyId);

    /**
     * 여정의 최대 sequence 값 조회
     */
    @Query("SELECT COALESCE(MAX(jr.sequence), 0) FROM JourneyRouteEntity jr WHERE jr.journey.id = :journeyId")
    Integer findMaxSequenceByJourneyId(@Param("journeyId") Long journeyId);

    /**
     * 여정의 최소 sequence 값 조회
     */
    @Query("SELECT COALESCE(MIN(jr.sequence), 0) FROM JourneyRouteEntity jr WHERE jr.journey.id = :journeyId")
    Integer findMinSequenceByJourneyId(@Param("journeyId") Long journeyId);
}