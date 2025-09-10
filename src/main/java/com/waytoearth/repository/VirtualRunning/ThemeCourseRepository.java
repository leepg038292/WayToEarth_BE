package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.ThemeCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 운영자 제공 테마 코스 Repository
 */
public interface ThemeCourseRepository extends JpaRepository<ThemeCourseEntity, Long> {
    
    // N+1 문제 해결을 위한 fetch join 쿼리 추가
    @Query("SELECT tc FROM ThemeCourseEntity tc LEFT JOIN FETCH tc.segments WHERE tc.id = :id")
    Optional<ThemeCourseEntity> findByIdWithSegments(@Param("id") Long id);
}
