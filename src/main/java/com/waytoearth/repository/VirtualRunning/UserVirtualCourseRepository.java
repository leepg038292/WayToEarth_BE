package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.UserVirtualCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVirtualCourseRepository extends JpaRepository<UserVirtualCourseEntity, Long> {
    List<UserVirtualCourseEntity> findByUserId(Long userId);
}