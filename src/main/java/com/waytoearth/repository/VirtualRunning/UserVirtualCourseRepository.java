package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.UserVirtualCourseEntity;
import com.waytoearth.entity.enums.VirtualCourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserVirtualCourseRepository extends JpaRepository<UserVirtualCourseEntity, Long> {
    List<UserVirtualCourseEntity> findByUserId(Long userId);

    /**
     * 원자적 총 거리 업데이트
     */
    @Modifying
    @Query("UPDATE UserVirtualCourseEntity uvc " +
           "SET uvc.totalDistanceAccumulated = uvc.totalDistanceAccumulated + :additionalDistance " +
           "WHERE uvc.id = :id")
    int addTotalDistanceAtomically(@Param("id") Long id, 
                                   @Param("additionalDistance") Double additionalDistance);

    /**
     * 진행률 및 상태 업데이트
     */
    @Modifying
    @Query("UPDATE UserVirtualCourseEntity uvc " +
           "SET uvc.progressPercent = :progressPercent, " +
           "    uvc.status = :status " +
           "WHERE uvc.id = :id")
    int updateProgressAndStatus(@Param("id") Long id,
                               @Param("progressPercent") Double progressPercent,
                               @Param("status") VirtualCourseStatus status);
}