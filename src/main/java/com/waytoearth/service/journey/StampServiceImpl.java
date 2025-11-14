package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.StampCollectRequest;
import com.waytoearth.dto.response.journey.StampResponse;
import com.waytoearth.entity.journey.LandmarkEntity;
import com.waytoearth.entity.journey.StampEntity;
import com.waytoearth.entity.journey.UserJourneyProgressEntity;
import com.waytoearth.exception.UnauthorizedAccessException;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.StampRepository;
import com.waytoearth.repository.journey.UserJourneyProgressRepository;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StampServiceImpl implements StampService {

    private final StampRepository stampRepository;
    private final UserJourneyProgressRepository progressRepository;
    private final LandmarkRepository landmarkRepository;

    @Override
    @Transactional
    public StampResponse collectStamp(AuthenticatedUser user, StampCollectRequest request) {
        UserJourneyProgressEntity progress = progressRepository.findById(request.progressId())
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + request.progressId()));

        if (!progress.getUser().getId().equals(user.getUserId())) {
            throw new UnauthorizedAccessException("본인의 여정에 대해서만 스탬프를 수집할 수 있습니다.");
        }

        LandmarkEntity landmark = landmarkRepository.findById(request.landmarkId())
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + request.landmarkId()));

        // 이미 수집한 스탬프인지 확인
        if (stampRepository.findByUserJourneyProgressIdAndLandmarkId(request.progressId(), request.landmarkId()).isPresent()) {
            throw new IllegalArgumentException("이미 수집한 스탬프입니다.");
        }

        // 진행률 확인 (랜드마크에 도달했는지)
        // 가상 여행 컨셉 - 실제 GPS 위치가 아닌 누적 거리로만 판정 (해외 여정 지원)
        if (progress.getCurrentDistanceKm() < landmark.getDistanceFromStart()) {
            throw new IllegalArgumentException(
                String.format("아직 이 랜드마크에 도달하지 않았습니다. (현재: %.2fkm, 필요: %.2fkm)",
                    progress.getCurrentDistanceKm(), landmark.getDistanceFromStart())
            );
        }

        // 스탬프 생성
        StampEntity stamp = StampEntity.builder()
                .userJourneyProgress(progress)
                .landmark(landmark)
                .stampImageUrl(generateStampImageUrl(landmark))
                .build();

        StampEntity savedStamp = stampRepository.save(stamp);

        log.info("스탬프 수집 완료: userId={}, landmarkId={}",
                progress.getUser().getId(), landmark.getId());

        return StampResponse.from(savedStamp);
    }

    @Override
    public List<StampResponse> getStampsByUserId(Long userId) {
        List<StampEntity> stamps = stampRepository.findByUserIdWithLandmark(userId);

        return stamps.stream()
                .map(StampResponse::from)
                .toList();
    }

    @Override
    public List<StampResponse> getStampsByProgressId(AuthenticatedUser user, Long progressId) {
        UserJourneyProgressEntity progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + progressId));

        if (!progress.getUser().getId().equals(user.getUserId())) {
            throw new UnauthorizedAccessException("본인의 여정에 대해서만 스탬프를 조회할 수 있습니다.");
        }

        List<StampEntity> stamps = stampRepository.findByUserJourneyProgressIdWithLandmark(progressId);

        return stamps.stream()
                .map(StampResponse::from)
                .toList();
    }


    @Override
    public boolean canCollectStamp(AuthenticatedUser user, Long progressId, Long landmarkId) {
        UserJourneyProgressEntity progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + progressId));

        if (!progress.getUser().getId().equals(user.getUserId())) {
            throw new UnauthorizedAccessException("본인의 여정에 대해서만 수집 가능 여부를 확인할 수 있습니다.");
        }

        LandmarkEntity landmark = landmarkRepository.findById(landmarkId)
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + landmarkId));

        // 중복 수집 체크
        boolean alreadyCollected = stampRepository
                .findByUserJourneyProgressIdAndLandmarkId(progressId, landmarkId)
                .isPresent();

        if (alreadyCollected) {
            return false;
        }

        // 진행률 기반으로만 체크 (실제 위치 검증 제거)
        return progress.getCurrentDistanceKm() >= landmark.getDistanceFromStart();
    }

    @Override
    public StampStatistics getStampStatistics(Long userId) {
        Long totalStamps = stampRepository.countStampsByUserId(userId);
        Long completedJourneys = progressRepository.countCompletedJourneysByUserId(userId);

        return new StampStatistics(totalStamps, completedJourneys);
    }


    /**
     * 스탬프 이미지 URL 생성
     */
    private String generateStampImageUrl(LandmarkEntity landmark) {
        // 실제로는 랜드마크에 따른 고유한 스탬프 이미지를 반환
        return String.format("https://waytoearth.com/stamps/landmark_%d.png", landmark.getId());
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
