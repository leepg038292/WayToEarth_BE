package com.waytoearth.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.dto.response.feed.FeedResponse;
import com.waytoearth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import static com.waytoearth.entity.QFeed.feed;
import static com.waytoearth.entity.QUser.user;
import static com.waytoearth.entity.QRunningRecord.runningRecord;
import static com.waytoearth.entity.QFeedLike.feedLike;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FeedResponse> findFeedsWithUserAndLikeStatus(User currentUser, Pageable pageable) {
        // 단일 쿼리로 모든 필요한 데이터를 조회하여 N+1 문제 해결
        var results = queryFactory
                .select(
                        feed.id,
                        feed.content,
                        feed.imageUrl,
                        feed.likeCount,
                        feedLike.isNotNull(), // 현재 사용자의 좋아요 여부
                        feed.createdAt,
                        user.id,
                        user.nickname,
                        user.profileImageUrl,
                        runningRecord.distance,
                        runningRecord.duration,
                        runningRecord.averagePaceSeconds,
                        runningRecord.calories
                )
                .from(feed)
                .join(feed.user, user)  // User 정보 조인
                .leftJoin(feed.runningRecord, runningRecord)  // RunningRecord 정보 조인
                .leftJoin(feedLike).on(feedLike.feed.eq(feed).and(feedLike.user.eq(currentUser)))  // 현재 사용자의 좋아요 여부
                .orderBy(feed.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 수동으로 FeedResponse 객체 생성
        return results.stream()
                .map(tuple -> FeedResponse.builder()
                        .id(tuple.get(feed.id))
                        .content(tuple.get(feed.content))
                        .imageUrl(tuple.get(feed.imageUrl))
                        .likeCount(tuple.get(feed.likeCount))
                        .liked(Boolean.TRUE.equals(tuple.get(feedLike.isNotNull())))
                        .createdAt(tuple.get(feed.createdAt))
                        .userId(tuple.get(user.id))
                        .nickname(tuple.get(user.nickname))
                        .profileImageUrl(tuple.get(user.profileImageUrl))
                        .distance(tuple.get(runningRecord.distance) != null ? 
                                tuple.get(runningRecord.distance).doubleValue() : null)
                        .duration(tuple.get(runningRecord.duration))
                        .averagePace(formatPace(tuple.get(runningRecord.averagePaceSeconds)))
                        .calories(tuple.get(runningRecord.calories))
                        .build())
                .toList();
    }

    private String formatPace(Integer paceSeconds) {
        if (paceSeconds == null) return null;
        int minutes = paceSeconds / 60;
        int seconds = paceSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
