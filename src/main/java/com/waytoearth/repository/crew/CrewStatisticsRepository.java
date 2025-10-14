package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewStatisticsRepository extends JpaRepository<CrewStatisticsEntity, Long>, CrewStatisticsRepositoryCustom {

    Optional<CrewStatisticsEntity> findByCrewAndMonth(CrewEntity crew, String month);

    List<CrewStatisticsEntity> findByCrewOrderByMonthDesc(CrewEntity crew);

    List<CrewStatisticsEntity> findByCrewAndMonthBetweenOrderByMonth(
            CrewEntity crew, String startMonth, String endMonth);

    //MVP 사용자 포함 조회
    @Query("SELECT cs FROM CrewStatisticsEntity cs " +
           "LEFT JOIN FETCH cs.mvpUser " +
           "WHERE cs.crew = :crew AND cs.month = :month")
    Optional<CrewStatisticsEntity> findByCrewAndMonthWithMvp(@Param("crew") CrewEntity crew,
                                                            @Param("month") String month);

    //기본적인 존재 여부 확인
    boolean existsByCrewAndMonth(CrewEntity crew, String month);

    //원자적 통계 업데이트 (Lost Update 방지)
    @Modifying
    @Query("UPDATE CrewStatisticsEntity cs SET " +
           "cs.runCount = cs.runCount + :runCount, " +
           "cs.totalDistance = cs.totalDistance + :distance, " +
           "cs.activeMembers = CASE WHEN :isNewActiveMember = true THEN cs.activeMembers + 1 ELSE cs.activeMembers END " +
           "WHERE cs.crew.id = :crewId AND cs.month = :month")
    int updateStatisticsAtomically(@Param("crewId") Long crewId, @Param("month") String month,
                                  @Param("runCount") int runCount, @Param("distance") BigDecimal distance,
                                  @Param("isNewActiveMember") boolean isNewActiveMember);

    //원자적 평균 페이스 업데이트 (복잡한 계산은 별도 처리)
    @Modifying
    @Query("UPDATE CrewStatisticsEntity cs SET cs.avgPaceSeconds = :newAvgPace " +
           "WHERE cs.crew.id = :crewId AND cs.month = :month")
    int updateAveragePace(@Param("crewId") Long crewId, @Param("month") String month,
                         @Param("newAvgPace") BigDecimal newAvgPace);

    //월간 MVP 업데이트
    @Modifying
    @Query("UPDATE CrewStatisticsEntity cs SET cs.mvpUser = :mvpUser, cs.mvpDistance = :mvpDistance " +
           "WHERE cs.crew.id = :crewId AND cs.month = :month")
    int updateMvpUser(@Param("crewId") Long crewId, @Param("month") String month,
                      @Param("mvpUser") User mvpUser, @Param("mvpDistance") BigDecimal mvpDistance);

    //크루 현재 멤버 수 원자적 증가/감소
    @Modifying
    @Query("UPDATE CrewEntity c SET c.currentMembers = c.currentMembers + :delta " +
           "WHERE c.id = :crewId AND c.currentMembers + :delta >= 0 AND c.currentMembers + :delta <= c.maxMembers")
    int updateCurrentMembersAtomically(@Param("crewId") Long crewId, @Param("delta") int delta);

    //크루 삭제 시 모든 통계 삭제
    @Modifying
    @Query("DELETE FROM CrewStatisticsEntity cs WHERE cs.crew.id = :crewId")
    void deleteAllByCrewId(@Param("crewId") Long crewId);
}