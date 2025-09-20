package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.GuestbookCreateRequest;
import com.waytoearth.dto.response.journey.GuestbookResponse;
import com.waytoearth.entity.Journey.GuestbookEntity;
import com.waytoearth.entity.Journey.LandmarkEntity;
import com.waytoearth.entity.User.User;
import com.waytoearth.repository.journey.GuestbookRepository;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.StampRepository;
import com.waytoearth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GuestbookServiceImpl implements GuestbookService {

    private final GuestbookRepository guestbookRepository;
    private final UserRepository userRepository;
    private final LandmarkRepository landmarkRepository;
    private final StampRepository stampRepository;

    @Override
    @Transactional
    public GuestbookResponse createGuestbook(Long userId, GuestbookCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        LandmarkEntity landmark = landmarkRepository.findById(request.landmarkId())
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + request.landmarkId()));

        // 방명록 생성
        GuestbookEntity guestbook = GuestbookEntity.builder()
                .user(user)
                .landmark(landmark)
                .message(request.message())
                .photoUrl(request.photoUrl())
                .isPublic(request.isPublic() != null ? request.isPublic() : true)
                .build();

        GuestbookEntity savedGuestbook = guestbookRepository.save(guestbook);

        log.info("방명록 작성 완료: userId={}, landmarkId={}, isPublic={}",
                userId, request.landmarkId(), savedGuestbook.getIsPublic());

        return GuestbookResponse.from(savedGuestbook);
    }

    @Override
    public Page<GuestbookResponse> getGuestbookByLandmark(Long landmarkId, Pageable pageable) {
        Page<GuestbookEntity> guestbooks = guestbookRepository.findPublicGuestbookByLandmarkId(landmarkId, pageable);

        return guestbooks.map(GuestbookResponse::from);
    }



    @Override
    public List<GuestbookResponse> getUserGuestbook(Long userId) {
        List<GuestbookEntity> guestbooks = guestbookRepository.findByUserIdWithLandmark(userId);

        return guestbooks.stream()
                .map(GuestbookResponse::from)
                .toList();
    }

    @Override
    public Page<GuestbookResponse> getRecentGuestbook(Pageable pageable) {
        Page<GuestbookEntity> guestbooks = guestbookRepository.findRecentPublicGuestbook(pageable);

        return guestbooks.map(GuestbookResponse::from);
    }

    @Override
    public LandmarkStatistics getLandmarkStatistics(Long landmarkId) {
        Long totalGuestbook = guestbookRepository.countByLandmarkIdAndIsPublicTrue(landmarkId);
        Long totalVisitors = stampRepository.countCollectorsByLandmarkId(landmarkId);

        return new LandmarkStatistics(
                totalGuestbook,
                totalVisitors
        );
    }
}