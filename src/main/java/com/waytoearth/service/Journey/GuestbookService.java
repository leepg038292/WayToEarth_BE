package com.waytoearth.service.Journey;

import com.waytoearth.dto.request.journey.GuestbookCreateRequest;
import com.waytoearth.dto.response.journey.GuestbookResponse;
import com.waytoearth.entity.Journey.GuestbookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GuestbookService {

    /**
     * 방명록 작성
     */
    GuestbookResponse createGuestbook(Long userId, GuestbookCreateRequest request);

    /**
     * 랜드마크별 공개 방명록 목록 조회 (페이징)
     */
    Page<GuestbookResponse> getGuestbookByLandmark(Long landmarkId, Pageable pageable);



    /**
     * 사용자별 방명록 목록 조회
     */
    List<GuestbookResponse> getUserGuestbook(Long userId);

    /**
     * 최근 방명록 조회 (전체)
     */
    Page<GuestbookResponse> getRecentGuestbook(Pageable pageable);

    /**
     * 랜드마크 통계 정보
     */
    LandmarkStatistics getLandmarkStatistics(Long landmarkId);

    /**
     * 랜드마크 통계 레코드
     */
    record LandmarkStatistics(
        Long totalGuestbook,
        Long totalVisitors
    ) {}
}