package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.ProgressUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProgressUpdateLogRepository extends JpaRepository<ProgressUpdateLog, String> {

    /**
     * 중복 요청 체크 (동일한 세션, 세그먼트, 거리로 최근 요청이 있는지)
     */
    @Query("SELECT COUNT(p) > 0 FROM ProgressUpdateLog p " +
           "WHERE p.sessionId = :sessionId " +
           "AND p.segmentId = :segmentId " +
           "AND p.distanceKm = :distanceKm " +
           "AND p.createdAt > :since")
    boolean existsDuplicateRequest(@Param("sessionId") String sessionId,
                                  @Param("segmentId") Long segmentId,
                                  @Param("distanceKm") Double distanceKm,
                                  @Param("since") LocalDateTime since);

    /**
     * 오래된 로그 삭제 (배치 작업용)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
