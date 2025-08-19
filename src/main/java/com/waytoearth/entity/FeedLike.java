package com.waytoearth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder   // ✅ 추가
@Table(name = "feed_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"feed_id", "user_id"})
})
public class FeedLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FeedLike(Feed feed, User user) {
        this.feed = feed;
        this.user = user;
    }
}
