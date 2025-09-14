package com.waytoearth.repository.Feed;

import com.waytoearth.entity.Feed.Feed;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedRepositoryCustom {
    List<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // N+1 문제 해결을 위한 fetch join 쿼리
    @Query("SELECT f FROM Feed f " +
           "JOIN FETCH f.user " +
           "LEFT JOIN FETCH f.runningRecord " +
           "WHERE f.id = :feedId")
    Optional<Feed> findByIdWithUserAndRecord(@Param("feedId") Long feedId);
    
    // 동시성 문제 해결을 위한 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Feed f WHERE f.id = :feedId")
    Optional<Feed> findByIdWithLock(@Param("feedId") Long feedId);

    // 최신 좋아요 수만 조회
    @Query("SELECT f.likeCount FROM Feed f WHERE f.id = :feedId")
    int getLikeCount(@Param("feedId") Long feedId);
    
    // 원자적 좋아요 증가 (동시성 안전)
    @Modifying
    @Query("UPDATE Feed f SET f.likeCount = f.likeCount + 1 WHERE f.id = :feedId")
    int incrementLikeCount(@Param("feedId") Long feedId);
    
    // 원자적 좋아요 감소 (동시성 안전)
    @Modifying
    @Query("UPDATE Feed f SET f.likeCount = f.likeCount - 1 WHERE f.id = :feedId")
    int decrementLikeCount(@Param("feedId") Long feedId);
    
    @Query("SELECT f FROM Feed f " +
           "JOIN FETCH f.user " +
           "LEFT JOIN FETCH f.runningRecord " +
           "ORDER BY f.createdAt DESC")
    List<Feed> findAllWithUserAndRecordOrderByCreatedAtDesc(Pageable pageable);
}
