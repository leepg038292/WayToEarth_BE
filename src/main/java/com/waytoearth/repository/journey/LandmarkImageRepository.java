package com.waytoearth.repository.journey;

import com.waytoearth.entity.journey.LandmarkImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandmarkImageRepository extends JpaRepository<LandmarkImage, Long> {
    List<LandmarkImage> findByLandmarkIdOrderByOrderIndexAsc(Long landmarkId);
}

