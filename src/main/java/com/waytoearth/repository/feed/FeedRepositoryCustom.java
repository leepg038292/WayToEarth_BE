package com.waytoearth.repository.feed;

import com.waytoearth.dto.response.feed.FeedResponse;
import com.waytoearth.entity.user.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedRepositoryCustom {
    /**
     * N+1 문제 해결을 위한 최적화된 피드 목록 조회
     * - 단일 쿼리로 User, RunningRecord, 좋아요 여부를 모두 조회
     */
    List<FeedResponse> findFeedsWithUserAndLikeStatus(User currentUser, Pageable pageable);
}
