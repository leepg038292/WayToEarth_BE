package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.SegmentProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SegmentProgressRepository extends JpaRepository<SegmentProgressEntity, Long> {
    List<SegmentProgressEntity> findByUserVirtualCourseId(Long userVirtualCourseId);
}