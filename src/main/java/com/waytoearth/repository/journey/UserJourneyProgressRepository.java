package com.waytoearth.repository.journey;

import com.waytoearth.entity.journey.UserJourneyProgressEntity;
import com.waytoearth.entity.enums.JourneyProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJourneyProgressRepository extends JpaRepository<UserJourneyProgressEntity, Long>, UserJourneyProgressRepositoryCustom {

    /**
     * 진행 ID로 조회 (Journey 정보 포함)
     */
    @Query("SELECT ujp FROM UserJourneyProgressEntity ujp JOIN FETCH ujp.journey WHERE ujp.id = :progressId")
    Optional<UserJourneyProgressEntity> findByIdWithJourney(@Param("progressId") Long progressId);

    /**
     * 사용자별 여행 진행 목록 조회
     */
    @Query("SELECT ujp FROM UserJourneyProgressEntity ujp JOIN FETCH ujp.journey WHERE ujp.user.id = :userId ORDER BY ujp.createdAt DESC")
    List<UserJourneyProgressEntity> findByUserIdWithJourney(@Param("userId") Long userId);

    /**
     * 사용자의 특정 여행 진행 상태 조회
     */
    Optional<UserJourneyProgressEntity> findByUserIdAndJourneyId(Long userId, Long journeyId);

    /**
     * 사용자의 특정 여행 진행 상태 조회 (Journey 정보 포함)
     */
    @Query("SELECT ujp FROM UserJourneyProgressEntity ujp JOIN FETCH ujp.journey WHERE ujp.user.id = :userId AND ujp.journey.id = :journeyId")
    Optional<UserJourneyProgressEntity> findByUserIdAndJourneyIdWithJourney(@Param("userId") Long userId, @Param("journeyId") Long journeyId);

    /**
     * 활성 상태인 사용자 여행 진행 목록
     */
    List<UserJourneyProgressEntity> findByUserIdAndStatus(Long userId, JourneyProgressStatus status);

    /**
     * 완료된 여행 목록 조회
     */
    @Query("SELECT ujp FROM UserJourneyProgressEntity ujp JOIN FETCH ujp.journey WHERE ujp.user.id = :userId AND ujp.status = 'COMPLETED' ORDER BY ujp.completedAt DESC")
    List<UserJourneyProgressEntity> findCompletedJourneysByUserId(@Param("userId") Long userId);

    /**
     * 진행률별 여행 조회
     */
    List<UserJourneyProgressEntity> findByUserIdAndProgressPercentGreaterThanEqualOrderByProgressPercentDesc(
            Long userId, Double progressPercent);

    /**
     * 세션 ID로 진행 조회
     */
    Optional<UserJourneyProgressEntity> findBySessionId(String sessionId);

    /**
     * 세션 ID로 진행 조회 (Journey 정보 포함)
     */
    @Query("SELECT ujp FROM UserJourneyProgressEntity ujp JOIN FETCH ujp.journey WHERE ujp.sessionId = :sessionId")
    Optional<UserJourneyProgressEntity> findBySessionIdWithJourney(@Param("sessionId") String sessionId);

    /**
     * 사용자의 총 완주한 여행 수
     */
    @Query("SELECT COUNT(ujp) FROM UserJourneyProgressEntity ujp WHERE ujp.user.id = :userId AND ujp.status = 'COMPLETED'")
    Long countCompletedJourneysByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 총 누적 거리
     */
    @Query("SELECT COALESCE(SUM(ujp.currentDistanceKm), 0) FROM UserJourneyProgressEntity ujp WHERE ujp.user.id = :userId")
    Double getTotalDistanceByUserId(@Param("userId") Long userId);

    /**
     * 특정 여정의 완주자 수
     */
    @Query("SELECT COUNT(ujp) FROM UserJourneyProgressEntity ujp WHERE ujp.journey.id = :journeyId AND ujp.status = 'COMPLETED'")
    Long countCompletedRunnersByJourneyId(@Param("journeyId") Long journeyId);

    /**
     * 특정 여정을 진행 중이거나 완료한 모든 러너 수 (함께 뛰는 사람)
     * ACTIVE 또는 COMPLETED 상태인 고유 사용자 수 반환
     */
    @Query("SELECT COUNT(DISTINCT ujp.user.id) FROM UserJourneyProgressEntity ujp WHERE ujp.journey.id = :journeyId AND ujp.status IN ('ACTIVE', 'COMPLETED')")
    Long countActiveOrCompletedRunnersByJourneyId(@Param("journeyId") Long journeyId);

    /**
     * 사용자 ID로 여행 진행 내역 일괄 삭제 (회원 탈퇴용)
     */
    void deleteByUserId(Long userId);
}