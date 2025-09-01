package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.CustomCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 사용자 커스텀 코스 Repository
 */
public interface CustomCourseRepository extends JpaRepository<CustomCourseEntity, Long> {
    List<CustomCourseEntity> findByUserId(Long userId);
}
