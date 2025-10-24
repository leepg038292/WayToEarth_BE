package com.waytoearth.repository.journey;

import com.waytoearth.entity.journey.StoryCardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryCardImageRepository extends JpaRepository<StoryCardImage, Long> {
    List<StoryCardImage> findByStoryCardIdOrderByOrderIndexAsc(Long storyCardId);
}

