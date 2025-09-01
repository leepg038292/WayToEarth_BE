package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.CourseSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 코스 세그먼트 Repository
 */
public interface CourseSegmentRepository extends JpaRepository<CourseSegmentEntity, Long> {

    // 테마 코스 기준 조회
    List<CourseSegmentEntity> findByThemeCourseIdOrderByOrderIndex(Long themeCourseId);

    // 커스텀 코스 기준 조회
    List<CourseSegmentEntity> findByCustomCourseIdOrderByOrderIndex(Long customCourseId);
}
