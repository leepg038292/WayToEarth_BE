package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.SegmentProgressEntity;
import com.waytoearth.entity.enums.VirtualCourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SegmentProgressRepository extends JpaRepository<SegmentProgressEntity, Long> {
    List<SegmentProgressEntity> findByUserVirtualCourseId(Long userVirtualCourseId);
    
    /**
     * 특정 사용자 코스의 특정 세그먼트 진행 정보를 조회
     */
    @Query("SELECT sp FROM SegmentProgressEntity sp " +
           "WHERE sp.userVirtualCourse.id = :userVirtualCourseId " +
           "AND sp.segmentId = :segmentId")
    Optional<SegmentProgressEntity> findByUserVirtualCourseIdAndSegmentId(
        @Param("userVirtualCourseId") Long userVirtualCourseId,
        @Param("segmentId") Long segmentId);

    /**
     * 원자적 거리 업데이트 (동시성 안전)
     */
    @Modifying
    @Query("UPDATE SegmentProgressEntity sp " +
           "SET sp.distanceAccumulated = sp.distanceAccumulated + :additionalDistance " +
           "WHERE sp.userVirtualCourse.id = :userVirtualCourseId " +
           "AND sp.segmentId = :segmentId")
    int addDistanceAtomically(@Param("userVirtualCourseId") Long userVirtualCourseId,
                             @Param("segmentId") Long segmentId,
                             @Param("additionalDistance") Double additionalDistance);

    /**
     * 세그먼트 완료 상태 업데이트
     */
    @Modifying
    @Query("UPDATE SegmentProgressEntity sp " +
           "SET sp.status = :status " +
           "WHERE sp.userVirtualCourse.id = :userVirtualCourseId " +
           "AND sp.segmentId = :segmentId " +
           "AND sp.distanceAccumulated >= " +
           "    (SELECT cs.distanceKm FROM CourseSegmentEntity cs WHERE cs.id = :segmentId)")
    int updateSegmentStatusIfCompleted(@Param("userVirtualCourseId") Long userVirtualCourseId,
                                      @Param("segmentId") Long segmentId,
                                      @Param("status") VirtualCourseStatus status);
}