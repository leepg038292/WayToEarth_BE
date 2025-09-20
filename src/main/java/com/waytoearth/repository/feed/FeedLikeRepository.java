package com.waytoearth.repository.feed;

import com.waytoearth.entity.feed.Feed;
import com.waytoearth.entity.feed.FeedLike;
import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    boolean existsByFeedAndUser(Feed feed, User user);
    Optional<FeedLike> findByFeedAndUser(Feed feed, User user);
    int countByFeed(Feed feed);
    void deleteByFeedAndUser(Feed feed, User user);
    void deleteByFeed(Feed feed);

}
