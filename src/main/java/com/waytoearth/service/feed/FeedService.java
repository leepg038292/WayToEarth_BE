package com.waytoearth.service.feed;

import com.waytoearth.dto.request.feed.FeedCreateRequest;
import com.waytoearth.dto.response.feed.FeedLikeResponse;
import com.waytoearth.dto.response.feed.FeedResponse;
import com.waytoearth.entity.Feed.Feed;
import com.waytoearth.entity.Feed.FeedLike;
import com.waytoearth.entity.Running.RunningRecord;
import com.waytoearth.entity.User.User;
import com.waytoearth.repository.Feed.FeedLikeRepository;
import com.waytoearth.repository.Feed.FeedRepository;
import com.waytoearth.repository.Running.RunningRecordRepository;
import com.waytoearth.repository.User.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final UserRepository userRepository;
    private final RunningRecordRepository runningRecordRepository;
    private final FileService fileService;

    /**
     * 피드 작성
     */
    @Transactional
    public FeedResponse createFeed(AuthenticatedUser authUser, FeedCreateRequest req) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        RunningRecord record = runningRecordRepository.findById(req.getRunningRecordId())
                .orElseThrow(() -> new RuntimeException("Running record not found"));

        Feed feed = Feed.builder()
                .user(user)
                .runningRecord(record)
                .content(req.getContent())
                .imageUrl(req.getImageUrl())
                .imageKey(req.getImageKey())
                .build();

        feedRepository.save(feed);

        return FeedResponse.from(feed, false); // 작성 직후 liked=false
    }

    /**
     * 피드 목록 조회 (N+1 문제 해결된 버전)
     */
    @Transactional(readOnly = true)
    public List<FeedResponse> getFeeds(AuthenticatedUser authUser, int offset, int limit) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 단일 쿼리로 모든 데이터를 조회하여 N+1 문제 해결
        return feedRepository.findFeedsWithUserAndLikeStatus(user, PageRequest.of(offset / limit, limit));
    }

    /**
     * 피드 단건 조회 (N+1 문제 해결된 버전)
     */
    @Transactional(readOnly = true)
    public FeedResponse getFeed(AuthenticatedUser authUser, Long feedId) {
        // fetch join으로 연관 엔티티를 한 번에 조회
        Feed feed = feedRepository.findByIdWithUserAndRecord(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));
        
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 단일 쿼리로 좋아요 여부 확인
        boolean liked = feedLikeRepository.existsByFeedAndUser(feed, user);
        return FeedResponse.from(feed, liked);
    }

    /**
     * 피드 삭제
     */
    @Transactional
    public void deleteFeed(AuthenticatedUser authUser, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        if (!feed.getUser().getId().equals(authUser.getUserId())) {
            throw new IllegalStateException("본인이 작성한 피드만 삭제할 수 있습니다.");
        }

        // 피드 삭제 전에 관련된 모든 좋아요 데이터 먼저 삭제
        feedLikeRepository.deleteByFeed(feed);

        //  S3 삭제
        if (feed.getImageKey() != null) {
            fileService.deleteObject(feed.getImageKey());
        }

        feedRepository.delete(feed);
    }

    /**
     * 피드 좋아요 (토글) - 동시성 문제 해결
     */
    @Transactional
    public FeedLikeResponse toggleLike(AuthenticatedUser authUser, Long feedId) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        return feedLikeRepository.findByFeedAndUser(feed, user)
                .map(existingLike -> {
                    // 좋아요 취소
                    feedLikeRepository.delete(existingLike);
                    feedRepository.decrementLikeCount(feedId);

                    int newCount = feedRepository.getLikeCount(feedId); //  최신값 강제 조회
                    return new FeedLikeResponse(feed.getId(), newCount, false);
                })
                .orElseGet(() -> {
                    // 좋아요 추가
                    FeedLike like = FeedLike.builder()
                            .feed(feed)
                            .user(user)
                            .build();
                    feedLikeRepository.save(like);
                    feedRepository.incrementLikeCount(feedId);

                    int newCount = feedRepository.getLikeCount(feedId); //  최신값 강제 조회
                    return new FeedLikeResponse(feed.getId(), newCount, true);
                });
    }

}
