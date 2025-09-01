package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.ThemeCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 운영자 제공 테마 코스 Repository
 */
public interface ThemeCourseRepository extends JpaRepository<ThemeCourseEntity, Long> {
}
