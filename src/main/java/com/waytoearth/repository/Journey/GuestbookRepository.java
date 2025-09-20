package com.waytoearth.repository.Journey;

import com.waytoearth.entity.Journey.GuestbookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestbookRepository extends JpaRepository<GuestbookEntity, Long> {

    /**
     * 랜드마크별 공개 방명록 목록 (페이징)
     */
    @Query("SELECT g FROM GuestbookEntity g JOIN FETCH g.user WHERE g.landmark.id = :landmarkId AND g.isPublic = true ORDER BY g.createdAt DESC")
    Page<GuestbookEntity> findPublicGuestbookByLandmarkId(@Param("landmarkId") Long landmarkId, Pageable pageable);

    /**
     * 사용자별 방명록 목록
     */
    @Query("SELECT g FROM GuestbookEntity g JOIN FETCH g.landmark WHERE g.user.id = :userId ORDER BY g.createdAt DESC")
    List<GuestbookEntity> findByUserIdWithLandmark(@Param("userId") Long userId);



    /**
     * 랜드마크별 방명록 총 개수
     */
    Long countByLandmarkIdAndIsPublicTrue(Long landmarkId);

    /**
     * 사용자의 총 방명록 수
     */
    Long countByUserId(Long userId);

    /**
     * 최근 방명록 조회 (전체)
     */
    @Query("SELECT g FROM GuestbookEntity g JOIN FETCH g.user JOIN FETCH g.landmark WHERE g.isPublic = true ORDER BY g.createdAt DESC")
    Page<GuestbookEntity> findRecentPublicGuestbook(Pageable pageable);
}