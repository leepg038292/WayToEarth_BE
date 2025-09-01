package com.waytoearth.repository.VirtualRunning;

import com.waytoearth.entity.VirtualRunning.SegmentLandmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 세그먼트 랜드마크 Repository
 */
public interface SegmentLandmarkRepository extends JpaRepository<SegmentLandmarkEntity, Long> {
    List<SegmentLandmarkEntity> findBySegmentId(Long segmentId);
}
