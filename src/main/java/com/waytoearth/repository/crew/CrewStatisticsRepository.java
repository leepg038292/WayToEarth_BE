package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}